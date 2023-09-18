package com.melowetty.hsepermhelper.dto

import com.melowetty.hsepermhelper.entity.UserEntity
import com.melowetty.hsepermhelper.models.UserEventType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class UserEventDto(
    @Schema(description = "ID ивента")
    val id: Long,

    @Schema(description = "Время и дата выполнения ивента")
    val date: LocalDateTime = LocalDateTime.now(),

    @Schema(description = "Какой пользователь вызвал ивент")
    val targetUser: UserEntity,

    @Schema(description = "Тип ивента")
    val userEventType: UserEventType,
)