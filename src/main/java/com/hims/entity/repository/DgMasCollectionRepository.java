package com.hims.entity.repository;

import com.hims.entity.DgMasCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DgMasCollectionRepository extends JpaRepository<DgMasCollection,Long> {



//
    List<DgMasCollection> findByStatusOrderByCollectionNameAsc(String y);



    List<DgMasCollection> findAllByOrderByStatusDescLastChgTimeDescLastChgDateDesc();
}
