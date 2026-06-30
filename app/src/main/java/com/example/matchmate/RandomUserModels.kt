package com.example.matchmate

data class RandomUserResponse(
    val results: List<RandomUserDto>
)

data class RandomUserDto(
    val name: UserNameDto,
    val dob: UserDobDto,
    val location: UserLocationDto,
    val picture: UserPictureDto,
    val email: String
)

data class UserNameDto(
    val first: String,
    val last: String
)

data class UserDobDto(
    val age: Int
)

data class UserLocationDto(
    val city: String,
    val country: String
)

data class UserPictureDto(
    val large: String
)
