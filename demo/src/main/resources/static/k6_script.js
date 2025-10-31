import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

// - /files/bytes/{id}
// - /files/resource/{id}
// - /files/streaming/{id}
// - /files/servlet/{id}
// 중 하나를 골라서 테스트하면 된다.

// 지연시간 테스트
export const options = {
    vus: 5,                 // 낮은 동시성: 서버 내부 처리 속도 확인용
    duration: '30s',        // 일정 시간 유지
    thresholds: {
        http_req_duration: ['p(95)<200'],
        http_req_failed: ['rate<0.01'],
    },
    noConnectionReuse: false, // keep-alive ON → 핸드셰이크 비용 제외
};

// 처리량 안정성 테스트
// export const options = {
//     stages: [
//         { duration: '10s', target: 20 }, // 워밍업
//         { duration: '60s', target: 100 }, // 본 테스트
//         { duration: '10s', target: 0 },   // 쿨다운
//     ],
//     thresholds: {
//         http_req_duration: ['p(95)<250'],
//         http_req_failed: ['rate<0.01'],
//     },
//     noConnectionReuse: true, // Keep-Alive 켜기
// };

// 테스트 대상 엔드포인트 선택
const baseHost = 'http://localhost:8080';
const basePath = '/files/streaming';
// 다른 후보:
// const basePath = '/files/bytes';
// const basePath = '/files/resource';
// const basePath = '/files/servlet';

// ID 목록 준비

const ids = new SharedArray('ids', function () {
    const text = open('./ids.txt');
    return text
        .split('\n')
        .map(line => line.trim())
        .filter(Boolean);
});

// 세션당 몇 개씩 요청할지 (유저 시점에서 카메라가 시야 내 타일 여러 개 요청한다고 가정)
const tilesPerSession = 30;

export default function () {
    // 무작위 시작 위치 골라서 연속된 id들을 요청
    const baseIndex = Math.floor(Math.random() * (ids.length - tilesPerSession));

    for (let i = 0; i < tilesPerSession; i++) {
        const id = ids[baseIndex + i];

        const url = `${baseHost}${basePath}/${id}`;
        const res = http.get(url, {
            headers: {
                'Connection': 'keep-alive',
                // 'Cache-Control': 'no-cache', // 캐시 우회시 키기
            },
            timeout: '30s',
        });

        check(res, {
            'status 200': r => r.status === 200
        });

        // API 요청 간 약간의 텀
        sleep(Math.random() * 0.05);
    }

    // 세션 간 텀 (사용자 API 요청 텀)
    sleep(1);
}
