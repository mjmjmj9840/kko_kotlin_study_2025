package kr.study.elan.kotlin.service

import kr.study.elan.kotlin.domain.User
import kr.study.elan.kotlin.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    fun add(createUser: User) {
        userRepository.save(createUser)
    }

    fun findById(name: String): User {
        return userRepository.findById(name)
    }

    fun findAll(size: Int = 15): Collection<User> {
        return userRepository.findAll(size)
    }
}