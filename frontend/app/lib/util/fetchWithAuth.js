const BASE_URL = "http://localhost:8080";

const refreshAccessToken = async () => {
    try {
        const response = await fetch(`${BASE_URL}/oauth/token/refresh`, {
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
    }
};

const fetchWithAuth = async (url, options = {}, retry = true) => {
    try {
        // 세션 스토리지에서 user_id와 access_token 가져오기
        const userId = sessionStorage.getItem('user_id');
        const accessToken = sessionStorage.getItem('access_token');

        // user_id가 없으면 로그인 화면으로 리디렉션
        if (!userId) {
            console.warn('사용자 ID 없음, 로그인 화면으로 이동합니다.');
            if (window.location.pathname !== '/login') {
                window.location.href = '/login';
            }
            return; // 함수 종료
        }

        // user_id만 있고 access_token이 없으면 일반 fetch 실행
        if (userId && !accessToken) {
            console.warn('액세스 토큰 없음, 일반 fetch 실행');
            return await fetch(url, options);
        }

        // user_id와 access_token 둘 다 있으면 Authorization 헤더 추가하여 fetchWithAuth 실행
        const headers = {
            ...options.headers,
            'Authorization': `Bearer ${accessToken}`,
        };

        // Content-Type 헤더 추가 (FormData가 아닌 경우에만)
        if (!(options.body instanceof FormData)) {
            headers['Content-Type'] = 'application/json';
        }

        const response = await fetch(url, {
            ...options,
            headers,
        });

        // 401 또는 403 상태 처리
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
        }

        return response;
    } catch (error) {
        console.error('API 호출 오류:', error);
    }
};

export {fetchWithAuth};