package com.melowetty.hsepermhelper.service.impl

import Schedule
import com.melowetty.hsepermhelper.dto.UserDto
import com.melowetty.hsepermhelper.events.EventType
import com.melowetty.hsepermhelper.events.ScheduleChangedEvent
import com.melowetty.hsepermhelper.events.UsersChangedEvent
import com.melowetty.hsepermhelper.exceptions.ScheduleNotFoundException
import com.melowetty.hsepermhelper.models.Lesson
import com.melowetty.hsepermhelper.models.ScheduleFileLinks
import com.melowetty.hsepermhelper.repository.ScheduleRepository
import com.melowetty.hsepermhelper.service.ScheduleService
import com.melowetty.hsepermhelper.service.UserFilesService
import com.melowetty.hsepermhelper.service.UserService
import com.melowetty.hsepermhelper.utils.FileUtils
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.util.*

@Service
class ScheduleServiceImpl(
    private val scheduleRepository: ScheduleRepository,
    private val userService: UserService,
    private val userFilesService: UserFilesService,
    private val env: Environment
): ScheduleService {
    init {
        refreshScheduleFiles()
    }
    private fun filterSchedules(schedules: List<Schedule>, user: UserDto): List<Schedule> {
        val filteredSchedules = mutableListOf<Schedule>()
        schedules.forEach { schedule ->
            val filteredLessons = schedule.lessons.flatMap { it.value }.filter { lesson: Lesson ->
                if (lesson.subGroup != null) lesson.group == user.settings?.group
                        && lesson.subGroup == user.settings.subGroup
                else lesson.group == user.settings?.group
            }
            val groupedLessons = filteredLessons.groupBy { it.date }
            filteredSchedules.add(
                schedule.copy(
                    lessons = groupedLessons
                )
            )
        }
        return filteredSchedules
    }

    override fun getUserSchedulesByTelegramId(telegramId: Long): List<Schedule> {
        val user = userService.getByTelegramId(telegramId)
        return filterSchedules(scheduleRepository.getSchedules(), user)
    }

    override fun getUserSchedulesById(id: UUID): List<Schedule> {
        val user = userService.getById(id)
        return filterSchedules(scheduleRepository.getSchedules(), user)
    }


    override fun getScheduleResource(id: UUID): Resource {
        val schedules = getUserSchedulesById(id)
        return FileUtils.convertSchedulesToCalendarFile(schedules)
    }

    @EventListener
    fun handleUsersChanging(event: UsersChangedEvent) {
        if(event.type == EventType.ADDED) {
            refreshScheduleFile(user = event.source)
        }
        else if(event.type == EventType.EDITED) {
            refreshScheduleFile(user = event.source)
        }
    }

    @EventListener
    fun handleScheduleChanging(event: ScheduleChangedEvent) {
        refreshScheduleFiles()
    }

    override fun getScheduleFileByTelegramId(baseUrl: String, telegramId: Long): ScheduleFileLinks {
        if (getUserSchedulesByTelegramId(telegramId).isEmpty()) throw ScheduleNotFoundException("Расписание для пользователя не найдено!")
        val user = userService.getByTelegramId(telegramId)
        val link = "${baseUrl}${env.getProperty("server.servlet.context-path")}/files/user_files/${user.id}/${SCHEDULE_FILE}"
        return ScheduleFileLinks(
            linkForDownload = link,
            linkForRemoteCalendar = "webcal://${link}"
        )
    }

    override fun getAvailableCourses(): List<Int> {
        return scheduleRepository.getAvailableCourses()
    }

    override fun getAvailablePrograms(course: Int): List<String> {
        return scheduleRepository.getAvailablePrograms(course = course)
    }

    override fun getAvailableGroups(course: Int, program: String): List<String> {
        return scheduleRepository.getAvailableGroups(course = course, program = program)
    }

    override fun getAvailableSubgroups(course: Int, program: String, group: String): List<Int> {
        return scheduleRepository.getAvailableSubgroups(course = course, program = program, group = group)
    }

    final override fun refreshScheduleFiles() {
        userService.getAllUsers().forEach {
            refreshScheduleFile(user = it)
        }
    }

    override fun refreshScheduleFile(user: UserDto) {
        userFilesService.storeFile(user, getScheduleResource(user.id), SCHEDULE_FILE)
    }

    companion object {
        const val SCHEDULE_FILE = "schedule.ics"
    }

}