# 프로젝트 구조
```
com.example.anika
├── AnikaApplication.kt
├── config
│   └── PokeApiProperties.kt
│   └── WebClientConfig.kt
└── pokemon
├── adapter
│   ├── incoming
│   │   └── web
│   │       └── ControllerExceptionHandler.kt
│   │       └── GetPokemonResponse.kt
│   │       └── PokemonController.kt    
│   └── outcoming
│       └── web
│           ├── PokeApiAdapter.kt
│           └── PokeApiResponse.kt
├── application
│   ├── port
│   │   ├── incoming
│   │   │   └── PokemonUseCase.kt
│   │   └── outcoming
│   │       └── PokemonPort.kt
│   └── service
│       └── PokemonService.kt
└── domain
└── Pokemon.kt
└── Stat.kt
```