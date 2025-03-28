package app.daos;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import app.exceptions.DaoException;

public abstract class AbstractDao<T, I> {
    protected final EntityManagerFactory emf;
    protected final Class<T> entityClass;

    protected AbstractDao(Class<T> entityClass, EntityManagerFactory emf) {
        this.entityClass = entityClass;
        this.emf = emf;
    }

    public T create(T t) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(t);
            em.getTransaction().commit();
            return t;
        } catch (Exception e) {
            throw new DaoException("Error in create(): " + t.getClass());
        }
    }

    public T findById(Object id) {
        try (EntityManager em = emf.createEntityManager()) {
            T entity = em.find(entityClass, id);
            if (entity == null) {
                throw new DaoException("Error in finding " + entityClass + " with id: " + id);
            }
            return entity;
        } catch (Exception e) {
            throw new DaoException("Error in finding " + entityClass + " with id: " + id);
        }
    }

    public List<T> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT t FROM " + entityClass.getSimpleName() + " t";
            return em.createQuery(jpql, entityClass).getResultList();
        } catch (Exception e) {
            throw new DaoException("Error in finding all of :" + entityClass);
        }
    }

    public T update(T t) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            T mergedT = em.merge(t);
            em.getTransaction().commit();
            return mergedT;
        } catch (Exception e) {
            throw new DaoException("Error in updating :" + t);
        }
    }

    public void delete(Object id) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "DELETE FROM " + entityClass.getSimpleName() + " t WHERE t.id = :id";
            em.getTransaction().begin();
            em.createQuery(jpql)
                    .setParameter("id", id)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new DaoException("Error in delting " + entityClass + " with id: " + id);
        }
    }
}