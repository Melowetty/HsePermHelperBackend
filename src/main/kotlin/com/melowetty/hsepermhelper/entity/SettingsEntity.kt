package com.melowetty.hsepermhelper.entity

import jakarta.persistence.*

@Entity
@Table(name = "settings")
data class SettingsEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    val id: Long? = null,

    @Column(name = "user_group")
    val group: String = "",

    @Column(name = "user_sub_group")
    val subGroup: Int = 0,

    @Column(name = "include_quarter_schedule")
    val includeQuarterSchedule: Boolean? = false,

    @Column(name = "include_common_english")
    val includeCommonEnglish: Boolean? = false,

    @Column(name = "include_common_minor")
    val includeCommonMinor: Boolean? = true,

    @Column(name = "is_enabled_remote_calendar")
    val isEnabledRemoteCalendar: Boolean? = false,
)