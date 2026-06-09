import { create } from 'zustand';

export interface SensorData {
    deviceId: string;
    sensorType: string;
    value: number;
    unit: string;
    recordedAt: string;
}

interface SensorState {
    sensors: Record<string, SensorData>;
    history: Record<string, SensorData[]>;
    updateSensor: (data: SensorData) => void;
    loadHistory: (dataList: SensorData[]) => void;
}

export const useSensorStore = create<SensorState>((set) => ({
    sensors: {},
    history: {},
    updateSensor: (data) =>
        set((state) => {
            const key = `${data.deviceId}:${data.sensorType}`;
            const prevHistory = state.history[key] ?? [];
            return {
                sensors: { ...state.sensors, [key]: data },
                history: {
                    ...state.history,
                    [key]: [...prevHistory.slice(-59), data],
                },
            };
        }),
    loadHistory: (dataList) =>
        set(() => {
            const sensors: Record<string, SensorData> = {};
            const history: Record<string, SensorData[]> = {};
            for (const data of dataList) {
                const key = `${data.deviceId}:${data.sensorType}`;
                history[key] = [...(history[key] ?? []), data];
                sensors[key] = data;
            }
            return { sensors, history };
        }),
}));
