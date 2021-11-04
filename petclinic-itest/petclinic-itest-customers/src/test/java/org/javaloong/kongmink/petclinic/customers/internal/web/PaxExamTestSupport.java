package org.javaloong.kongmink.petclinic.customers.internal.web;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.File;

import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.configurationFolder;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public abstract class PaxExamTestSupport {

    @Inject
    BundleContext context;

    @Inject
    ClientBuilder clientBuilder;

    public static <T> T getService(BundleContext bundleContext, Class<T> type) {
        ServiceReference<T> serviceReference = bundleContext.getServiceReference(type);
        return bundleContext.getService(serviceReference);
    }

    public <T> T getService(Class<T> type) {
        return getService(context, type);
    }

    public WebTarget webTarget() {
        return webTarget("http://localhost:8080/api/");
    }

    public WebTarget webTarget(String uri) {
        return clientBuilder.build()
                .register(new JacksonJsonProvider())
                .target(uri);
    }

    @Configuration
    public Option[] config() {
        return new Option[]{
                baseOptions(),
                hsqldb(),
                ariesJpa(),
                hibernate(),
                ariesJaxRSWhiteboard(),
                ariesJaxRSWhiteboardJackson(),
                testBundles(),

                //felix osgi
                mavenBundle("org.apache.felix", "org.apache.felix.configadmin")
                        .versionAsInProject().startLevel(START_LEVEL_SYSTEM_BUNDLES),
                mavenBundle("org.apache.felix", "org.apache.felix.scr")
                        .versionAsInProject().startLevel(START_LEVEL_SYSTEM_BUNDLES),
        };
    }

    protected Option baseOptions() {
        return composite(
                // Settings for the OSGi 4.3 Weaving
                // By default, we will not weave any classes. Change this setting to include classes
                // that you application needs to have woven.
                systemProperty("org.apache.aries.proxy.weaving.enabled").value("none"),
                // This gives a fast fail when any bundle is unresolved
                systemProperty("pax.exam.osgi.unresolved.fail").value("true"),
                workingDirectory("target/pax-exam"),
                logback(),
                junit(),
                dbUnit(),
                configurationFolder(new File("src/test/resources/config"))
        );
    }

    protected Option logback() {
        return composite(
                systemProperty("logback.configurationFile").value("src/test/resources/logback.xml"),
                url("link:classpath:META-INF/links/org.slf4j.api.link"),
                url("link:classpath:META-INF/links/ch.qos.logback.core.link"),
                url("link:classpath:META-INF/links/ch.qos.logback.classic.link"));
    }

    protected Option junit() {
        return composite(
                junitBundles(),
                mavenBundle("org.assertj", "assertj-core").versionAsInProject(),
                mavenBundle("org.awaitility", "awaitility").versionAsInProject()
        );
    }

    protected Option dbUnit() {
        return composite(
                mavenBundle("commons-collections", "commons-collections").versionAsInProject(),
                wrappedBundle(mavenBundle("org.dbunit", "dbunit").versionAsInProject()),
                wrappedBundle(mavenBundle("com.github.database-rider", "rider-core").versionAsInProject())
        );
    }

    protected Option testBundles() {
        return composite(
                mavenBundle("org.apache.geronimo.specs", "geronimo-validation_2.0_spec", "1.1"),
                mavenBundle("org.glassfish", "jakarta.el").versionAsInProject(),
                mavenBundle("org.apache.commons", "commons-lang3").versionAsInProject(),
                mavenBundle("org.modelmapper", "modelmapper").versionAsInProject(),

                mavenBundle("org.javaloong.kongmink", "petclinic-osgi-bean-validator").versionAsInProject(),
                mavenBundle("org.javaloong.kongmink", "petclinic-osgi-rest").versionAsInProject(),
                mavenBundle("org.javaloong.kongmink", "petclinic-osgi-customers-api").versionAsInProject(),
                mavenBundle("org.javaloong.kongmink", "petclinic-osgi-customers-ds").versionAsInProject()
        );
    }

    protected Option ariesJaxRSWhiteboardJackson() {
        return composite(
                mavenBundle("com.fasterxml.jackson.core", "jackson-core", "2.12.5"),
                mavenBundle("com.fasterxml.jackson.core", "jackson-annotations", "2.12.5"),
                mavenBundle("com.fasterxml.jackson.core", "jackson-databind", "2.12.5"),
                mavenBundle("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml", "2.12.5"),
                mavenBundle("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310", "2.12.5"),
                mavenBundle("com.fasterxml.jackson.jaxrs", "jackson-jaxrs-base", "2.12.5"),
                mavenBundle("com.fasterxml.jackson.jaxrs", "jackson-jaxrs-json-provider", "2.12.5"),
                mavenBundle("com.fasterxml.jackson.module", "jackson-module-jaxb-annotations", "2.12.5"),
                mavenBundle("org.yaml", "snakeyaml", "1.27"),
                mavenBundle("org.apache.aries.jax.rs", "org.apache.aries.jax.rs.jackson", "2.0.1")
        );
    }

    protected Option ariesJaxRSWhiteboard() {
        return composite(
                cxf(),
                mavenBundle("jakarta.xml.bind", "jakarta.xml.bind-api", "2.3.3"),
                mavenBundle("org.osgi", "org.osgi.util.function", "1.1.0"),
                mavenBundle("org.osgi", "org.osgi.util.promise", "1.1.1"),
                mavenBundle("org.osgi", "org.osgi.service.jaxrs", "1.0.0"),
                mavenBundle("org.apache.aries.component-dsl", "org.apache.aries.component-dsl.component-dsl", "1.2.2"),
                mavenBundle("org.apache.aries.jax.rs", "org.apache.aries.jax.rs.whiteboard", "2.0.1")
        );
    }

    protected Option cxf() {
        return composite(
                httpService(),
                systemPackage("javax.annotation;version=1.2"),
                mavenBundle("org.apache.aries.spec", "org.apache.aries.javax.jax.rs-api", "1.0.1"),
                mavenBundle("com.fasterxml.woodstox", "woodstox-core", "6.2.6"),
                mavenBundle("org.apache.ws.xmlschema", "xmlschema-core", "2.2.5"),
                mavenBundle("org.codehaus.woodstox", "stax2-api", "4.2.1"),
                mavenBundle("org.apache.cxf", "cxf-core", "3.4.5"),
                mavenBundle("org.apache.cxf", "cxf-rt-frontend-jaxrs", "3.4.5"),
                mavenBundle("org.apache.cxf", "cxf-rt-rs-client", "3.4.5"),
                mavenBundle("org.apache.cxf", "cxf-rt-rs-sse", "3.4.5"),
                mavenBundle("org.apache.cxf", "cxf-rt-security", "3.4.5"),
                mavenBundle("org.apache.cxf", "cxf-rt-transports-http", "3.4.5")
        );
    }

    protected Option httpService() {
        return composite(
                mavenBundle("org.apache.felix", "org.apache.felix.http.servlet-api", "1.1.2"),
                mavenBundle("org.apache.felix", "org.apache.felix.http.jetty", "4.1.12")
        );
    }

    protected Option hibernate() {
        return composite(
                transaction(),
                systemPackages("javax.xml.bind;version=2.2", "javax.xml.bind.annotation;version=2.2",
                        "javax.xml.bind.annotation.adapters;version=2.2"),
                systemPackages("javax.xml.stream;version=1.0", "javax.xml.stream.events;version=1.0",
                        "javax.xml.stream.util;version=1.0"),
                mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.antlr", "2.7.7_5"),
                mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.dom4j", "1.6.1_5"),
                mavenBundle("com.fasterxml", "classmate", "1.5.1"),
                mavenBundle("org.javassist", "javassist", "3.27.0-GA"),
                mavenBundle("net.bytebuddy", "byte-buddy", "1.10.10"),
                mavenBundle("org.jboss.logging", "jboss-logging", "3.4.1.Final"),
                mavenBundle("org.jboss", "jandex", "2.2.3.Final"),
                mavenBundle("org.hibernate.common", "hibernate-commons-annotations", "5.1.0.Final"),
                mavenBundle("org.hibernate", "hibernate-core", "5.4.32.Final"),
                mavenBundle("org.hibernate", "hibernate-osgi", "5.4.32.Final"),
                mavenBundle("jakarta.persistence", "jakarta.persistence-api").versionAsInProject()
        );
    }

    protected Option ariesJpa() {
        return composite(
                ariesProxy(),
                mavenBundle("org.apache.felix", "org.apache.felix.coordinator").versionAsInProject(),
                // jndi
                mavenBundle("org.apache.aries", "org.apache.aries.util", "1.1.3"),
                mavenBundle("org.apache.aries.jndi", "org.apache.aries.jndi.api", "1.1.0"),
                mavenBundle("org.apache.aries.jndi", "org.apache.aries.jndi.core", "1.0.2"),
                mavenBundle("org.apache.aries.jndi", "org.apache.aries.jndi.url", "1.1.0"),
                // blueprint
                mavenBundle("org.apache.aries.blueprint", "org.apache.aries.blueprint.api", "1.0.1"),
                mavenBundle("org.apache.aries.blueprint", "org.apache.aries.blueprint.core", "1.10.3"),
                // jpa
                mavenBundle("org.apache.aries.jpa", "org.apache.aries.jpa.api", "2.7.3"),
                mavenBundle("org.apache.aries.jpa", "org.apache.aries.jpa.container", "2.7.3"),
                mavenBundle("org.apache.aries.jpa", "org.apache.aries.jpa.support", "2.7.3"),
                mavenBundle("org.apache.aries.jpa.javax.persistence", "javax.persistence_2.1", "2.7.3")
        );
    }

    protected Option ariesProxy() {
        return composite(
                mavenBundle("org.ow2.asm", "asm", "9.2"),
                mavenBundle("org.ow2.asm", "asm-util", "9.2"),
                mavenBundle("org.ow2.asm", "asm-tree", "9.2"),
                mavenBundle("org.ow2.asm", "asm-analysis", "9.2"),
                mavenBundle("org.ow2.asm", "asm-commons", "9.2"),
                mavenBundle("org.apache.aries.proxy", "org.apache.aries.proxy", "1.1.11"),

                mavenBundle("org.apache.aries.spifly", "org.apache.aries.spifly.dynamic.bundle", "1.3.2")
        );
    }

    protected Option transaction() {
        return composite(
                // api
                mavenBundle("javax.interceptor", "javax.interceptor-api", "1.2.2"),
                mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.javax-inject", "1_3"),
                mavenBundle("javax.el", "javax.el-api", "3.0.0"),
                mavenBundle("javax.enterprise", "cdi-api", "1.2"),
                mavenBundle("javax.transaction", "javax.transaction-api", "1.2"),
                // tm
                mavenBundle("org.apache.aries.transaction", "org.apache.aries.transaction.manager", "1.3.3"),
                mavenBundle("org.apache.aries.transaction", "org.apache.aries.transaction.blueprint", "2.3.0")
        );
    }

    protected Option hsqldb() {
        return composite(
                systemPackage("javax.transaction;version=1.1.0"),
                systemPackage("javax.transaction.xa;version=1.1.0"),
                // just for DBCP2
                systemPackage("javax.transaction.xa;version=1.1.0;partial=true;mandatory:=partial"),
                mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.cglib", "3.3.0_1"),
                mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jasypt", "1.9.3_1"),
                mavenBundle("org.apache.commons", "commons-pool2", "2.11.1"),
                mavenBundle("org.apache.commons", "commons-dbcp2", "2.9.0"),
                mavenBundle("commons-logging", "commons-logging", "1.2"),
                mavenBundle("org.ops4j.pax.jdbc", "pax-jdbc", "1.5.0"),
                mavenBundle("org.ops4j.pax.jdbc", "pax-jdbc-hsqldb", "1.5.0"),
                mavenBundle("org.ops4j.pax.jdbc", "pax-jdbc-config", "1.5.0"),
                mavenBundle("org.ops4j.pax.jdbc", "pax-jdbc-pool-common", "1.5.0"),
                mavenBundle("org.ops4j.pax.jdbc", "pax-jdbc-pool-dbcp2", "1.5.0"),
                mavenBundle("org.hsqldb", "hsqldb").versionAsInProject(),
                mavenBundle("org.osgi", "org.osgi.service.jdbc").versionAsInProject()
        );
    }
}
