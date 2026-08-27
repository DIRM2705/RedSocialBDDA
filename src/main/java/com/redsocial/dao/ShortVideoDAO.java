/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.redsocial.dao;

import com.redsocial.model.Short_video; // Considera usar ShortVideo por convención de Java (CamelCase)
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class ShortVideoDao extends GenericDAO<Short_video, Long> {

    public ShortVideoDao() {
        super(Short_video.class);
    }

    // Método específico: Buscar videos por un hashtag o categoría
    public List<Short_video> findByHashtag(EntityManager em, String hashtag) {
        String jpql = "SELECT v FROM Short_video v WHERE v.description LIKE :hashtag";
        TypedQuery<Short_video> query = em.createQuery(jpql, Short_video.class);
        query.setParameter("hashtag", "%" + hashtag + "%");
        return query.getResultList();
    }
}