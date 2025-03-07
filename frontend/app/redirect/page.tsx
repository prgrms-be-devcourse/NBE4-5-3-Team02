'use client';

import { useRouter } from 'next/navigation';
import { useEffect } from 'react';

export default function RedirectPage() {
    const router = useRouter();

    useEffect(() => {
        const urlParams = new URLSearchParams(window.location.search);
        const authCode = urlParams.get('code');

        if (authCode) {
            router.push(`/login?code=${authCode}`);
        } else {
            console.error('인증 코드가 없습니다.');
            router.push('/login');
        }
    }, [router]);

    return <div>리다이렉트 중...</div>;
}