'use client';

import {
    LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from 'recharts';
import { useSensorStore } from '@/store/sensorStore';

interface Props {
    title: string;
    sensorType: string;
}

export default function SensorChart({ title, sensorType }: Props) {
    const { history } = useSensorStore();

    const data = Object.entries(history)
        .filter(([key]) => key.includes(sensorType))
        .flatMap(([, entries]) => entries)
        .map((d) => ({
            time: new Date(d.recordedAt).toLocaleTimeString('ko-KR'),
            value: d.value,
        }));

    return (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
            <h3 className="text-sm font-semibold text-gray-700 mb-4">{title}</h3>
            {data.length === 0 ? (
                <div className="flex items-center justify-center h-48 text-gray-400 text-sm">
                    데이터 없음
                </div>
            ) : (
                <ResponsiveContainer width="100%" height={200}>
                    <LineChart data={data}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="time" tick={{ fontSize: 11 }} />
                        <YAxis tick={{ fontSize: 11 }} />
                        <Tooltip />
                        <Line type="monotone" dataKey="value" stroke="#22c55e" dot={false} strokeWidth={2} />
                    </LineChart>
                </ResponsiveContainer>
            )}
        </div>
    );
}
