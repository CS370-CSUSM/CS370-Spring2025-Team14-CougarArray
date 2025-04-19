import asyncio
import websockets
import json

async def ping_websocket():
    uri = "ws://localhost:5666"
    async with websockets.connect(uri) as websocket:
        userInput = input(">")

        message = {"type": "EXECUTE", "execute": userInput }

        await websocket.send(json.dumps(message))
        print(f"Sent: {message}")

        # Optionally receive a response
        response = await websocket.recv()
        print(f"Received: {response}")

# Run the event loop
asyncio.run(ping_websocket())
