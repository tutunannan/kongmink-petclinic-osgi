package org.javaloong.kongmink.petclinic.customers.blueprint.impl.repository;

import org.javaloong.kongmink.petclinic.customers.model.Owner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;

public class OwnerRepositoryImpl implements OwnerRepository {

    @PersistenceContext(unitName = "customers")
    private EntityManager em;

    public OwnerRepositoryImpl() {

    }

    public OwnerRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Owner findById(int id) {
        return em.find(Owner.class, id);
    }

    @Override
    public void save(Owner owner) {
        if (owner.getId() == null) {
            em.persist(owner);
        } else {
            em.merge(owner);
            em.flush();
        }
    }

    @Override
    public void delete(Owner owner) {
        em.remove(em.contains(owner) ? owner : em.merge(owner));
    }

    @Override
    public Collection<Owner> findAll() {
        CriteriaQuery<Owner> query = em.getCriteriaBuilder().createQuery(Owner.class);
        return em.createQuery(query.select(query.from(Owner.class))).getResultList();
    }

    @Override
    public Collection<Owner> findAllByLastName(String lastName) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Owner> query = em.getCriteriaBuilder().createQuery(Owner.class);
        Root<Owner> root = query.from(Owner.class);
        return em.createQuery(query.select(root)
                .where(cb.like(root.get("lastName"), lastName + "%"))).getResultList();
    }
}
