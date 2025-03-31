# TheQuizz API

## Om
TheQuizz API er en vidensquiz med fokus på computer science som et proof of concept. På sigt kan quizzen udvides til at dække andre videnskabelige emner, eventuelt tilføjes der også gåder man kan løse enkeltvis. Formålet er at skabe et sjovt og engagerende quiz-format, der kan bruges i sociale sammenhænge, f.eks. som en aktivitet til en hyggelig aften. 

Det er designet til at man bruger en enhed(mobil/computer) til quizzen, og ikke flere.

Når proof of concept er færdigt, vil der blive tilføjet flere kategorier.

## Repositories
GitHub Repository: [TheQuizzAPI](https://github.com/mrPrimeBeef/TheQuizzAPI)

## Base URL
API’et kan tilgås på: [quizzapi.sem2.dk/api/routes](http://quizzapi.sem2.dk/api/routes)

## Endpoints

### Spil
| Metode | Endpoint | Request Body | Response | Beskrivelse |
|--------|---------|--------------|----------|-------------|
| `POST` | `/game/{number}` | `{}` | `201` | Opretter et nyt spil med et bestemt antal spillere. |
| `POST` | `/game/{gameid}/players/names` | `{ "player": "navn" }` | `(e1)` | Tilføjer spillere til et spil. |
| `GET` | `/game/{gameid}/questions?limit={numberOfQuestions}&category={category}&difficulty={difficulty}` | | `{ "game": {...} }` | Henter spørgsmål baseret på kategori og sværhedsgrad. |
| `POST` | `/api/game/{gameId}/player/{playerid}/question/{questionId}/answer` | `{ "answer": "Coding language" }` | `{ "correct": true, "pointsEarned": 10, "totalScore": 50 }` | Spilleren sender et svar. |
| `GET` | `/api/game/{gameId}/score` | | `{ "player": {...} }` | Henter stillingen for et igangværende spil. |

### Admin
| Metode | Endpoint | Request Body | Response | Beskrivelse |
|--------|---------|--------------|----------|-------------|
| `PUT` MANGLER | `/api/admin/question` | `{ "question": "Hvad står HTML for?", "rightAnswer": "HyperText Markup Language", "wrongAnswers": ["High Tech Modern Language", "Hyper Transfer Markup Language", "Home Tool Markup Language"], "category": "web development", "difficulty": "medium" }` | `{ "questionId": 25, "msg": "Question created successfully" }` | Opretter et nyt spørgsmål. |
| `PATCH` MANGLER | `/api/admin/question/{questionId}` | `{ "question": "Updated question?", "rightAnswer": "Updated answer" }` | `{ "msg": "Question updated successfully" }` | Opdaterer et spørgsmål. |
| `DELETE` MANGLER | `/api/admin/question/{questionId}` | | `{ "msg": "Question deleted successfully" }` | Sletter et spørgsmål. |

### Autentifikation
| Metode | Endpoint | Request Body | Response | Beskrivelse |
|--------|---------|--------------|----------|-------------|
| `POST` | `/auth/register` | `{ "username": "Jim", "password": "1234" }` | `200` | Registrerer en ny bruger. |
| `POST` | `/auth/login` | `{ "username": "Jim", "password": "1234" }` | `200` | Logger brugeren ind. |

## Fejlhåndtering
Alle fejl returneres i følgende format:
```json
{ "status": statusCode, "msg": "Beskrivelse af fejlen" }
```
| Fejlkode | Beskrivelse |
|----------|-------------|
| `400` | Ugyldig anmodning (manglende felt osv.). |
| `404` | Ressource ikke fundet (spil, spørgsmål osv.). |

## Request Body Eksempler
### Spilformat
```json
{
  "players": {
    "players": [
      { "name": "Player1", "points": 0 },
      { "name": "Player2", "points": 0 }
    ]
  },
  "questions": {
    "results": [
      {
        "difficulty": "EASY",
        "category": "Science: Computers",
        "question": "The series of the Intel HD graphics generation succeeding that of the 5000 and 6000 series (Broadwell) is called:",
        "correct_answer": "HD Graphics 500",
        "incorrect_answers": ["HD Graphics 700", "HD Graphics 600", "HD Graphics 7000"]
      }
    ]
  }
}
```
### Spillerformat
```json
{
  "players": [
    { "name": "Player1", "points": 0 },
    { "name": "Player2", "points": 0 }
  ]
}
```

## Kontakt
Har du spørgsmål eller feedback, kan du oprette en issue på [GitHub](https://github.com/mrPrimeBeef/TheQuizzAPI/issues).


projektet henter quizzes fra 

https://quizapi.io/categories
og
https://opentdb.com/api_config.php
