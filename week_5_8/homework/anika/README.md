# 프로젝트 구조
```
com.example.anika
├── AnikaApplication.kt
├── config
│   └── PokeApiProperties.kt
│   └── WebClientConfig.kt
└── pokemon
├── adapter
│   ├── in
│   │   └── web
│   │       └── ControllerExceptionHandler.kt
│   │       └── GetPokemonResponse.kt
│   │       └── PokemonController.kt    
│   └── out
│       └── web
│           ├── PokeApiAdapter.kt
│           └── PokeApiResponse.kt
├── application
│   ├── port
│   │   ├── in
│   │   │   └── PokemonUseCase.kt
│   │   └── out
│   │       └── PokemonPort.kt
│   └── service
│       └── PokemonService.kt
└── domain
└── Pokemon.kt
└── Stat.kt
```