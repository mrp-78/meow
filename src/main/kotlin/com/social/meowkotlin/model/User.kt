package com.social.meowkotlin.model

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "users")
class User : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "user_name")
    var userName: String? = null

    @Column(name = "phone_number")
    var phoneNumber: Long = 0

    constructor()

    constructor(userName: String?, phoneNumber: Long) {
        this.userName = userName
        this.phoneNumber = phoneNumber
    }
}
