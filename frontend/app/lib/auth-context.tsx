'use client'

import { createContext, useContext, useEffect, useState } from 'react'

type AuthContextType = {
    isLoggedIn: boolean
    login: () => void
    logout: () => Promise<void>
}

export const AuthContext = createContext<AuthContextType>(null!)

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [isLoggedIn, setIsLoggedIn] = useState(false)

    // 세션 스토리지 기반 인증 체크
    useEffect(() => {
        const checkAuth = async () => {
            const response = await fetch('/api/auth/check')
            const data = await response.json()
            if (data.isAuthenticated && sessionStorage.getItem('sessionActive')) {
                setIsLoggedIn(true)
            } else {
                sessionStorage.removeItem('sessionActive')
                setIsLoggedIn(false)
            }
        }
        checkAuth()
    }, [])

    // 창 종료 시 로그아웃 처리
    useEffect(() => {
        const handleUnload = () => {
            if (sessionStorage.getItem('sessionActive')) {
                navigator.sendBeacon('/api/auth/logout')
                sessionStorage.removeItem('sessionActive')
            }
        }

        window.addEventListener('beforeunload', handleUnload)
        return () => window.removeEventListener('beforeunload', handleUnload)
    }, [])

    const login = () => {
        sessionStorage.setItem('sessionActive', 'true')
        setIsLoggedIn(true)
    }

    const logout = async () => {
        await fetch('/api/auth/logout', { method: 'POST' })
        sessionStorage.removeItem('sessionActive')
        setIsLoggedIn(false)
    }

    return (
        <AuthContext.Provider value={{ isLoggedIn, login, logout }}>
            {children}
        </AuthContext.Provider>
    )
}

export const useAuth = () => useContext(AuthContext)