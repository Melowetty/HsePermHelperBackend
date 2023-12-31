package com.melowetty.hsepermhelper.models.v1

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.melowetty.hsepermhelper.models.LessonType
import com.melowetty.hsepermhelper.models.v2.LessonV2
import com.melowetty.hsepermhelper.models.ScheduleType
import com.melowetty.hsepermhelper.utils.DateUtils
import com.melowetty.hsepermhelper.utils.EmojiCode
import io.swagger.v3.oas.annotations.media.Schema
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.Description
import net.fortuna.ical4j.model.property.Uid
import net.fortuna.ical4j.util.RandomUidGenerator
import java.time.LocalDate
import java.time.LocalDateTime

data class LessonV1(
    @Schema(description = "Учебный предмет", example = "Программирование")
    val subject: String,
    @JsonIgnore val course: Int,
    @JsonIgnore val programme: String,
    @JsonIgnore val group: String,
    @JsonIgnore val subGroup: Int?,
    @JsonFormat(pattern = DateUtils.DATE_PATTERN)
    @Schema(description = "Дата пары", example = "03.09.2023", type = "string")
    val date: LocalDate,
    @Schema(description = "Время начала пары", example = "8:10")
    @JsonProperty("startTime")
    val startTimeStr: String,
    @Schema(description = "Время окончания пары", example = "9:30")
    @JsonProperty("endTime")
    val endTimeStr: String,
    @JsonIgnore val startTime: LocalDateTime,
    @JsonIgnore val endTime: LocalDateTime,
    @Schema(description = "Преподаватель", example = "Викентьева О.Л.", nullable = true)
    val lecturer: String?,
    @Schema(description = "Кабинет", example = "121", nullable = true)
    val office: String?,
    @Schema(description = "Корпус (если 0 - пара дистанционная)", example = "2", nullable = true)
    val building: Int?,
    @Schema(description = "Ссылки на пару (null - если ссылок нет)")
    val links: List<String>? = null,
    @Schema(description = "Дополнительная информация о паре (null - если информации нет)")
    val additionalInfo: List<String>? = null,
    @Schema(description = "Тип лекции", example = "SEMINAR")
    val lessonType: LessonType,
    @Schema(description = "Тип расписания-родителя", example = "COMMON_WEEK_SCHEDULE")
    val parentScheduleType: ScheduleType,
) : Comparable<LessonV2> {
    /**
     * Returns lesson will be in online mode
     *
     * @return true if lesson is online else false
     */
    fun isOnline(): Boolean {
        if(building == null && office == null) return false
        if(links?.isNotEmpty() == true) return true
        return (building == null || building == 0) && lessonType != LessonType.ENGLISH
    }

    /**
     * Converts lesson object to VEvent for import in calendar
     *
     * @return converted lesson to VEvent object
     */
    fun toVEvent(): VEvent {
        val additionalInfoContainingSymbol =
            if(additionalInfo?.isNotEmpty() == true) EmojiCode.ATTENTION_SYMBOL else ""
        val quarterScheduleSymbol =
            if(parentScheduleType == ScheduleType.QUARTER_SCHEDULE) "*" else ""
        val distantSymbol = if(isOnline()) EmojiCode.DISTANT_LESSON_SYMBOL else ""
        val event = VEvent(startTime, endTime,
            "${additionalInfoContainingSymbol}${distantSymbol}" +
                    "${lessonType.toEventSubject(subject)}${quarterScheduleSymbol}")
        val descriptionLines: MutableList<String> = mutableListOf()
        if (lecturer != null) {
            descriptionLines.add("Преподаватель: $lecturer")
        }
        if(isOnline()) {
            if (!links.isNullOrEmpty()) {
                descriptionLines.add("Ссылка на пару: ${links[0]}")
                if (links.size > 1) {
                    descriptionLines.add("Дополнительные ссылки на пару: ")
                    links.subList(1, links.size).forEach { descriptionLines.add(it) }
                }
            }
            else {
                descriptionLines.add("Место: онлайн")
            }
        } else {
            if (building == null && office == null) {
                if(lessonType == LessonType.COMMON_MINOR) {
                    descriptionLines.add("Информацию о времени и ссылке на майнор узнайте " +
                            "подробнее в HSE App X или в системе РУЗ")
                }
                else {
                    descriptionLines.add("Место: не указано")
                }
            }
            else {
                descriptionLines.add("Место: $building корпус - ${getOfficeStr()}")
            }
        }
        if (additionalInfo?.isNotEmpty() == true) {
            descriptionLines.add("\n" +
                    "Дополнительная информация: ${additionalInfo.joinToString("\n")}")
        }
        if(parentScheduleType == ScheduleType.QUARTER_SCHEDULE) {
            descriptionLines.add("\n" +
                    "* - пара взята из расписания на модуль, фактическое расписание " +
                    "может отличаться от этого")
        }
        event.add(Uid(RandomUidGenerator().generateUid().value))
        event.add(
            Description(
                descriptionLines.joinToString("\n")
            )
        )
        return event
    }

    private fun getOfficeStr(): String? {
        if(office == null) return null
        return if(office.toIntOrNull() == null) {
            if (office.contains(",")) return "кабинеты $office"
            office
        }
        else "кабинет $office"
    }

    override fun compareTo(other: LessonV2): Int {
        return date.compareTo(other.date)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LessonV1

        if (subject != other.subject) return false
        if (course != other.course) return false
        if (programme != other.programme) return false
        if (group != other.group) return false
        if (subGroup != other.subGroup) return false
        if (date != other.date) return false
        if (startTimeStr != other.startTimeStr) return false
        if (endTimeStr != other.endTimeStr) return false
        if (startTime != other.startTime) return false
        if (endTime != other.endTime) return false
        if (lecturer != other.lecturer) return false
        if (office != other.office) return false
        if (building != other.building) return false
        if (links != other.links) return false
        if (additionalInfo != other.additionalInfo) return false
        if (lessonType != other.lessonType) return false
        if (parentScheduleType != other.parentScheduleType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = subject.hashCode()
        result = 31 * result + course
        result = 31 * result + programme.hashCode()
        result = 31 * result + group.hashCode()
        result = 31 * result + (subGroup ?: 0)
        result = 31 * result + date.hashCode()
        result = 31 * result + startTimeStr.hashCode()
        result = 31 * result + endTimeStr.hashCode()
        result = 31 * result + startTime.hashCode()
        result = 31 * result + endTime.hashCode()
        result = 31 * result + (lecturer?.hashCode() ?: 0)
        result = 31 * result + (building?.hashCode() ?: 0)
        result = 31 * result + (office?.hashCode() ?: 0)
        result = 31 * result + (links?.hashCode() ?: 0)
        result = 31 * result + (additionalInfo?.hashCode() ?: 0)
        result = 31 * result + lessonType.hashCode()
        result = 31 * result + parentScheduleType.hashCode()
        return result
    }
}
