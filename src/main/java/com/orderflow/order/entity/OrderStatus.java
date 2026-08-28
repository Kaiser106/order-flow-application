package com.orderflow.order.entity;

public enum OrderStatus {
    PREPARING,      // Hazırlanıyor (İlk oluşturulduğunda bu olacak)
    PICKED_UP,      // Kurye teslim aldı
    ON_THE_WAY,     // Yolda
    DELIVERED,      // Teslim edildi
    CANCELLED       // İptal edildi
}