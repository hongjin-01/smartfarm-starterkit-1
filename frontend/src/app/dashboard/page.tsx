'use client';

import { useEffect } from 'react';
import { useWebSocket } from '@/lib/websocket';
import { useSensorStore } from '@/store/sensorStore';
import SensorCard from '@/components/dashboard/SensorCard';
import SensorChart from '@/components/dashboard/SensorChart';

export default function DashboardPage() {
    const { sensors, loadHistory } = useSensorStore();
    useWebSocket();

    useEffect(() => {
        fetch('/api/v1/sensors/recent?hours=24')
            .then((res) => res.json())
            .then((data) => loadHistory(data))
            .catch((err) => console.error('초기 센서 데이터 로딩 실패:', err));
    }, [loadHistory]);

    return (
        <div className="min-h-screen bg-gray-50 p-6">
            <header className="mb-8">
                <h1 className="text-2xl font-bold text-gray-900">스마트팜 대시보드</h1>
                <p className="text-gray-500 mt-1">실시간 복합환경 모니터링</p>
            </header>

            <section className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
                {Object.entries(sensors).map(([key, data]) => (
                    <SensorCard key={key} sensorKey={key} data={data} />
                ))}
                {Object.keys(sensors).length === 0 && (
                    <p className="col-span-4 text-center text-gray-400 py-12">
                        센서 데이터를 기다리는 중...
                    </p>
                )}
            </section>

            <section className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <SensorChart title="온도 추이" sensorType="temperature" />
                <SensorChart title="습도 추이" sensorType="humidity" />
            </section>
        </div>
    );
}
