package com.careerconnect.repository;

import com.careerconnect.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShareRepository extends JpaRepository<Share, Ids.UserPost> {

}
