package kr.study.elan.kotlin.repository

import kr.study.elan.kotlin.domain.User
import kr.study.elan.kotlin.exception.UserNotFoundException
import org.springframework.stereotype.Repository

@Repository
class UserRepository {
    private val users: MutableCollection<User> = mutableListOf()

    fun save(user: User) {
        users.add(user)
    }

    fun findById(id: String): User {
        val find = users.find { it.id == id }
        if (find == null) {
            throw UserNotFoundException("사용자를 찾을수 없습니다. [id=${id}]")
        }
        return find
    }

    fun findAll(size: Int): Collection<User> {
        return users.take(size)
    }
}