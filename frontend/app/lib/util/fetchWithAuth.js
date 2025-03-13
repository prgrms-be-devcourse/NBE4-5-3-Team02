const refreshAccessToken = async () => {
    try {
        const response = await fetch('http://localhost:8080/oauth/token/refresh', {
            method: 'POST',
            credentials: 'include', // 쿠키 포함
        });

        const data = await response.json();

        if (!response.ok || !data.data.access_token) {
            throw new Error('리프레시 토큰이 유효하지 않습니다.');
        }

        // 새 액세스 토큰을 세션 스토리지에 저장
        sessionStorage.setItem('access_token', data.data.access_token);

        return data.data.access_token; // 새 액세스 토큰 반환
    } catch (error) {
        console.error('토큰 갱신 실패:', error);
        sessionStorage.removeItem('access_token'); // 만료된 액세스 토큰 제거
        window.location.href = '/login'; // 로그인 페이지로 리디렉션
    }
};

const fetchWithAuth = async (url, options = {}, retry = true) => {
    try {
        // 세션 스토리지에서 액세스 토큰 가져오기
        const accessToken = sessionStorage.getItem('access_token');

        // Authorization 헤더 추가
        const headers = {
            'Authorization': `Bearer ${accessToken}`,
            ...options.headers,
        };

        if (!(options.body instanceof FormData)) {
            headers['Content-Type'] = 'application/json';
        }

        const response = await fetch(url, {
            ...options,
            headers,
        });

        if (response.status === 401 || response.status === 403) {
            if (retry) {
                console.warn('액세스 토큰 만료, 리프레시 토큰으로 갱신 시도 중...');

                // 새 액세스 토큰 발급
                const newAccessToken = await refreshAccessToken();

                if (newAccessToken) {
                    // 재시도: 새 액세스 토큰으로 요청 재전송
                    return fetchWithAuth(url, options, false);
                }
            }
            throw new Error('인증 실패: 다시 로그인하세요.');
        }

        return response;
    } catch (error) {
        console.error('API 호출 오류:', error);
        throw error;
    }
};

export { fetchWithAuth };