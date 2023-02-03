# ZIO-Messenger

Fun little project to play around with some ZIO concepts for the first time. An instant messenger-like app that 
randomly connects users into pairs to begin chatting.   

<img width="1499" alt="Screen Shot 2023-02-03 at 1 53 56 PM" src="https://user-images.githubusercontent.com/42941054/216621555-aa1f9462-e5cc-4cfd-9166-7eb1e58d4226.png">

#### Tech stack:
- ZIO: Functional effect management (https://github.com/zio/zio)
- ZIO-Http: Backend (https://github.com/zio/zio-http)
- ZIO-Actors: Functional concurrent state management (https://github.com/zio/zio-actors)
- ZIO-Quill: FRM db interaction layer (https://github.com/zio/zio-quill)



#### Setup:

Terminal 1: <br />
First, setup database and start server
```
% bash setup.sh                                                                                                                                                      [32/154]
...
Starting server on http://localhost:9000
```
Terminal 2: <br />
Second, log in and save login cookie
```
% curl -v  -X POST 'localhost:9000/login' -d '{"username":"elie"}'
...
set-cookie: Login Cookie={"username":"elie","userId":1,"creationDate":"2023-02-03T13:01:50.086445Z"}; Max-Age=9223372036; Expires=Sun, 16 May 2315 12:49:06 GMT
...
```
Save cookie (single quotes '' to wrap string)
```
% export cookie='Login Cookie={"username":"eliee","userId":1,"creationDate":"2023-02-03T13:01:50.086445Z"}; Max-Age=9223372036; Expires=Sun, 16 May 2315 12:49:06 GMT'
```
Thirdly, connect to server and wait to be paired with someone
```
% websocat ws://localhost:9000/chat-session -H "Cookie: $cookie"

Welcome elie - waiting to be connected with a pair
```
Terminal 3: (same process)
```
% curl -v  -X POST 'localhost:9000/login' -d '{"username":"eddie"}'
...
% export cookie=...
...
% websocat ws://localhost:9000/chat-session -H "Cookie: $cookie"

Connected - You are now chatting with elie
```
We also have an additional API to see message history (reverse chronological order)
```
% curl -X GET localhost:9000/messages/ed/ezra

["ezra: Literally nothing bro","ed: Wuu2?","ezra: Yes bro?","ed: Bro"]
```

#### Todo:
- Tests
- Recover 500 errors
- Group chat
- Frontend :D
