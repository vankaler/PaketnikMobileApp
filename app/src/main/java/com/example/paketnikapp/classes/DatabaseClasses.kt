package com.example.paketnikapp.classes

import java.time.ZonedDateTime

data class Client(val id: String, val name: String, val lastname: String, val email: String, val created: ZonedDateTime, val updated: ZonedDateTime)
data class Room(val id: String, val number: Number, val size: Number, val occupied: Boolean, val created: ZonedDateTime, val updated: ZonedDateTime)
data class Staff(val id: String, val name: String, val lastname: String, val email: String, val level: Number, val created: ZonedDateTime, val updated: ZonedDateTime)