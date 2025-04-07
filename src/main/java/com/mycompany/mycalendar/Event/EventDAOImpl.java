/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mycalendar.Event;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

/**
 *
 * @author ricky
 */
public class EventDAOImpl implements IEventDAO {

    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("MyCalendarPU");

    private EntityManager getEntityManger() {
        return emf.createEntityManager();
    }

    @Override
    public Event findById(int id) {
        EntityManager em = getEntityManger();
        try {
            return em.find(Event.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public void save(Event event) {
        EntityManager em = getEntityManger();
        try {
            em.getTransaction().begin();
            em.persist(event);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    @Override
    public void update(Event event) {
        EntityManager em = getEntityManger();
        try {
            em.getTransaction().begin();
            em.merge(event);
            em.getTransaction().commit();
            System.out.println("Record updated");
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Event event) {
        EntityManager em = getEntityManger();
        try {
            em.getTransaction().begin();
            Event managedEvent = em.merge(event);
            em.remove(managedEvent);
            em.getTransaction().commit();
            System.out.println("Event deleted");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Event> findAll() {
        EntityManager em = getEntityManger();
        try {
            TypedQuery<Event> query = em.createQuery("SELECT e FROM Event e", Event.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    //Close EMF when application shuts down
    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
