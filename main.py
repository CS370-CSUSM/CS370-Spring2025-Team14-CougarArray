import asyncio
import websockets
import json

async def ping_websocket():
    uri = "ws://localhost:5666"
    async with websockets.connect(uri) as websocket:
        userInput = input(">")
        message = {"type": "EXECUTE", "execute": userInput}

        await websocket.send(json.dumps(message))
        print(f"Sent: {message}")

        # Receive and parse the response as JSON
        response = await websocket.recv()
        try:
            data = json.loads(response)
            print(data["COMMENT"])  # Only print the "output" key
        except json.JSONDecodeError:
            print("Failed to parse JSON:", response)
        except KeyError:
            print("No 'output' key in response:", data)

# Run the event loop
asyncio.run(ping_websocket())
