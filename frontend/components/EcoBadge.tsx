'use client'
import { useEffect, useState } from 'react'
import { CloudRain, Trees } from 'lucide-react'

export default function EcoBadge() {
    const [co2Saved, setCo2Saved] = useState(0)

    useEffect(() => {
        // 가상의 CO2 절감량 계산 로직
        const timer = setInterval(() => {
            setCo2Saved(prev => prev + Math.random() * 0.1)
        }, 5000)
        return () => clearInterval(timer)
    }, [])

    return (
        <div className="flex items-center gap-3 bg-white/80 backdrop-blur-sm p-3 rounded-full shadow-sm">
            <div className="flex items-center gap-1 text-sm text-gray-800">
                <Trees className="w-4 h-4 text-green-600" /> {/* 초록색 적용 */}
                <span>{(co2Saved * 100).toFixed(0)}g CO₂ 절감</span>
            </div>
            <div className="h-4 w-px bg-gray-200" />
            <div className="flex items-center gap-1 text-sm text-gray-800">
                <CloudRain className="w-4 h-4 text-sky-500" /> {/* 하늘색 적용 */}
                <span>{(co2Saved * 50).toFixed(0)}ml 물 절약</span>
            </div>
        </div>
    )
}