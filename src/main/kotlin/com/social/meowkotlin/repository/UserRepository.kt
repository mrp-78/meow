package com.social.meowkotlin.repository

import com.social.meowkotlin.model.User
import org.springframework.data.jpa.repository.JpaRepository

@org.springframework.stereotype.Repository
interface UserRepository : JpaRepository<User?, Long?>
