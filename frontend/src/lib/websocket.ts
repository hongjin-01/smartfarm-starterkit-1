'use client';

import { useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useSensorStore } from '@/store/sensorStore';

export function useWebSocket() {
    const { updateSensor } = useSensorStore();

    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new SockJS('/ws'),
            reconnectDelay: 5000,
            onConnect: () => {
                client.subscribe('/topic/sensors', (message) => {
                    const data = JSON.parse(message.body);
                    updateSensor(data);
                });
            },
            onStompError: (frame) => {
                console.error('WebSocket 연결 오류:', frame);
            },
        });

        client.activate();
        return () => { client.deactivate(); };
    }, [updateSensor]);
}
