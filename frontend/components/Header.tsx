'use client'
import { useEffect, useState } from 'react'
import Link from 'next/link'
import { useAuth } from '@/app/lib/auth-context'
import AuthButton from '@/components/AuthButton'
import EcoBadge from "@/components/EcoBadge"

export default function Header() {
    const { isLoggedIn } = useAuth()
    const [loginTime, setLoginTime] = useState<string | null>(null)

    useEffect(() => {
        if (isLoggedIn) {
            const savedTime = localStorage.getItem('lastLoginTime')
            if (!savedTime) {
                const now = new Date().toISOString()
                localStorage.setItem('lastLoginTime', now)
            }
            setLoginTime(savedTime || new Date().toISOString())
        }
    }, [isLoggedIn])

    const formatTime = (isoString: string) => {
        return new Date(isoString).toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        })
    }

    return (
        <header className="flex justify-between items-center p-4 bg-white shadow-md">
            <Link
                href="/"
                className="text-2xl font-bold text-green-600 hover:text-green-700 transition-colors"
            >
                Toolgether
            </Link>

            <nav className="flex items-center gap-4">

                {isLoggedIn && loginTime && (
                    <span className="text-xs text-gray-500 ml-2">
            최근 접속: {formatTime(loginTime)}
          </span>
                )}

                <EcoBadge />
                <AuthButton />
            </nav>
        </header>
    )
}