package com.school.repository;
import com.school.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TicketRepository extends JpaRepository<Ticket,Long> {
    List<Ticket> findAllByOrderByDateCreationDesc();
    List<Ticket> findByStatut(String statut);
}
