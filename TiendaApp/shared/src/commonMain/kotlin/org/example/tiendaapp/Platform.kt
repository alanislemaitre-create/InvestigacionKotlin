package org.example.tiendaapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform