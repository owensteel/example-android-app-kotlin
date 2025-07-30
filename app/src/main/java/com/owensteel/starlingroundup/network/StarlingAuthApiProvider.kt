package com.owensteel.starlingroundup.network

interface StarlingAuthApiProvider {
    fun getAuthApi(): StarlingAuthApi
}