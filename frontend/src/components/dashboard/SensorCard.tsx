import { Thermometer, Droplets, Wind, Sun } from 'lucide-react';
import type { SensorData } from '@/store/sensorStore';

interface Props {
    sensorKey: string;
    data: SensorData;
}

const iconMap: Record<string, React.ElementType> = {
    temperature: Thermometer,
    humidity: Droplets,
    co2: Wind,
    light: Sun,
};

export default function SensorCard({ data }: Props) {
    const Icon = iconMap[data.sensorType] ?? Thermometer;

    return (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
            <div className="flex items-center justify-between mb-3">
                <span className="text-sm text-gray-500 truncate">{data.deviceId}</span>
                <Icon className="w-5 h-5 text-green-500 flex-shrink-0" />
            </div>
            <p className="text-2xl font-bold text-gray-900">
                {data.value.toFixed(1)}
                <span className="text-sm font-normal text-gray-500 ml-1">{data.unit}</span>
            </p>
            <p className="text-xs text-gray-400 mt-2 capitalize">{data.sensorType}</p>
        </div>
    );
}
