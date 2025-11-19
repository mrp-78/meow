package com.social.meowkotlin.model

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "posts")
class Post : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0

    @Column(name = "text")
    var text: String? = null

    @Column(name = "user_id")
    var userId: Long = 0

    constructor()

    constructor(text: String?, userId: Long) {
        this.text = text
        this.userId = userId
    }

    override fun toString(): String {
        return "Post [id=$id, text=$text, userId=$userId]"
    }
}
