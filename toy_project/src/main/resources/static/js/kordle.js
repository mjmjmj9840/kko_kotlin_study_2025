class KordleGame {
    constructor() {
        this.sessionId = null;
        this.nickname = '';
        this.maxAttempts = 6;
        this.attemptsLeft = 6;
        this.currentJamos = [];
        this.gameBoard = [];
        this.isGameOver = false;
        this.keyboardState = {};
        this.currentIndex = 0;
        
        // 한글 자모 키보드 레이아웃 (기본 자음 + 모음)
        this.keyboardLayout = [
            ['ㄱ', 'ㄴ', 'ㄷ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅅ'],
            ['ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'],
            ['ㅏ', 'ㅑ', 'ㅓ', 'ㅕ', 'ㅗ', 'ㅛ', 'ㅜ', 'ㅠ', 'ㅡ', 'ㅣ'],
            ['DELETE', 'ENTER']
        ];
        
        this.init();
    }

    init() {
        this.bindEvents();
        this.showStartScreen();
    }

    bindEvents() {
        // 시작 화면 이벤트
        document.getElementById('start-button').addEventListener('click', () => this.startGame());
        document.getElementById('stats-button').addEventListener('click', () => this.showStatsScreen());
        document.getElementById('dashboard-button').addEventListener('click', () => this.showDashboardScreen());
        
        // 게임 화면 이벤트
        document.getElementById('submit-guess').addEventListener('click', () => this.submitGuess());
        document.getElementById('new-game-button').addEventListener('click', () => this.showStartScreenWithNickname());
        document.getElementById('view-stats-button').addEventListener('click', () => this.showStatsScreen());
        document.getElementById('back-to-main-from-game').addEventListener('click', () => this.showStartScreenWithNickname());
        
        // 통계 화면 이벤트
        document.getElementById('back-to-game').addEventListener('click', () => this.showGameScreen());
        document.getElementById('back-to-main-from-stats').addEventListener('click', () => this.showStartScreenWithNickname());
        
        // 대시보드 화면 이벤트
        document.getElementById('back-to-start').addEventListener('click', () => this.showStartScreen());
        
        // 모달 이벤트
        document.getElementById('play-again').addEventListener('click', () => this.playAgain());
        document.getElementById('view-my-stats').addEventListener('click', () => this.showStatsFromModal());
        
        // 엔터 키 이벤트
        document.getElementById('nickname-input').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.startGame();
        });
    }

    showStartScreen() {
        this.hideAllScreens();
        document.getElementById('start-screen').classList.remove('hidden');
        document.getElementById('nickname-input').focus();
        this.resetGame();
    }

    showStartScreenWithNickname() {
        this.hideAllScreens();
        document.getElementById('start-screen').classList.remove('hidden');
        
        // 기존 닉네임 유지
        if (this.nickname) {
            document.getElementById('nickname-input').value = this.nickname;
        }
        
        document.getElementById('nickname-input').focus();
        this.resetGame();
    }

    showGameScreen() {
        this.hideAllScreens();
        document.getElementById('game-screen').classList.remove('hidden');
        this.createGameBoard();
        this.createJamoInputs();
        this.createKeyboard();
    }

    showStatsScreen() {
        this.hideAllScreens();
        document.getElementById('stats-screen').classList.remove('hidden');
        if (this.nickname) {
            this.loadStats();
        }
    }

    showDashboardScreen() {
        this.hideAllScreens();
        document.getElementById('dashboard-screen').classList.remove('hidden');
        this.loadDashboard();
    }

    hideAllScreens() {
        document.querySelectorAll('.screen').forEach(screen => {
            screen.classList.add('hidden');
        });
    }

    async startGame() {
        const nickname = document.getElementById('nickname-input').value.trim();
        if (!nickname) {
            alert('닉네임을 입력해주세요.');
            return;
        }

        this.nickname = nickname;
        
        try {
            const response = await fetch('/api/game/start', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ nickname: this.nickname })
            });

            if (response.ok) {
                const data = await response.json();
                this.sessionId = data.sessionId;
                this.maxAttempts = data.maxAttempts;
                this.attemptsLeft = this.maxAttempts;
                
                document.getElementById('player-name').textContent = `플레이어: ${this.nickname}`;
                this.updateAttemptsDisplay();
                
                this.showGameScreen();
            } else {
                alert('게임 시작에 실패했습니다.');
            }
        } catch (error) {
            console.error('Error starting game:', error);
            alert('게임 시작 중 오류가 발생했습니다.');
        }
    }

    createGameBoard() {
        const gameBoard = document.getElementById('game-board');
        gameBoard.innerHTML = '';
        
        for (let i = 0; i < this.maxAttempts; i++) {
            const row = document.createElement('div');
            row.className = 'guess-row';
            
            for (let j = 0; j < 6; j++) {
                const tile = document.createElement('div');
                tile.className = 'tile';
                tile.id = `tile-${i}-${j}`;
                row.appendChild(tile);
            }
            
            gameBoard.appendChild(row);
        }
    }

    createJamoInputs() {
        const currentJamos = document.getElementById('current-jamos');
        currentJamos.innerHTML = '';
        
        for (let i = 0; i < 6; i++) {
            const jamoInput = document.createElement('div');
            jamoInput.className = 'jamo-input';
            jamoInput.id = `jamo-${i}`;
            jamoInput.addEventListener('click', () => this.selectJamoInput(i));
            currentJamos.appendChild(jamoInput);
        }
        
        this.currentIndex = 0;
        this.updateJamoInputs();
    }

    selectJamoInput(index) {
        this.currentIndex = index;
        this.updateJamoInputs();
    }

    updateJamoInputs() {
        for (let i = 0; i < 6; i++) {
            const jamoInput = document.getElementById(`jamo-${i}`);
            jamoInput.classList.remove('active', 'filled');
            
            if (i === this.currentIndex) {
                jamoInput.classList.add('active');
            }
            
            if (this.currentJamos[i]) {
                jamoInput.textContent = this.currentJamos[i];
                jamoInput.classList.add('filled');
            } else {
                jamoInput.textContent = '';
            }
        }
    }

    createKeyboard() {
        const keyboard = document.getElementById('keyboard');
        keyboard.innerHTML = '';
        
        this.keyboardLayout.forEach(row => {
            const keyboardRow = document.createElement('div');
            keyboardRow.className = 'keyboard-row';
            
            row.forEach(key => {
                const keyElement = document.createElement('button');
                keyElement.className = 'key';
                keyElement.textContent = key;
                
                if (key === 'DELETE' || key === 'ENTER') {
                    keyElement.classList.add('action');
                }
                
                keyElement.addEventListener('click', () => this.handleKeyPress(key));
                keyboardRow.appendChild(keyElement);
            });
            
            keyboard.appendChild(keyboardRow);
        });
    }

    handleKeyPress(key) {
        if (this.isGameOver) return;
        
        if (key === 'DELETE') {
            this.deleteJamo();
        } else if (key === 'ENTER') {
            this.submitGuess();
        } else {
            this.addJamo(key);
        }
        
        this.updateJamoInputs();
    }

    addJamo(jamo) {
        if (this.currentIndex < 6) {
            this.currentJamos[this.currentIndex] = jamo;
            this.currentIndex = Math.min(5, this.currentIndex + 1);
        }
    }

    deleteJamo() {
        if (this.currentIndex > 0) {
            this.currentIndex--;
            this.currentJamos[this.currentIndex] = null;
        } else if (this.currentJamos[0]) {
            this.currentJamos[0] = null;
        }
    }

    async submitGuess() {
        // 빈 자모 필터링
        const filledJamos = this.currentJamos.filter(jamo => jamo != null);
        
        if (filledJamos.length !== 6) {
            alert('6개 자모를 모두 입력해주세요.');
            return;
        }

        if (this.isGameOver) return;

        try {
            const response = await fetch(`/api/game/${this.sessionId}/guess`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ jamos: filledJamos })
            });

            if (response.ok) {
                const data = await response.json();
                this.processGuessResult(filledJamos, data);
            } else if (response.status === 400) {
                alert('올바르지 않은 자모 조합입니다.');
            } else if (response.status === 409) {
                alert('게임이 이미 종료되었습니다.');
            } else {
                alert('추측 처리 중 오류가 발생했습니다.');
            }
        } catch (error) {
            console.error('Error submitting guess:', error);
            alert('네트워크 오류가 발생했습니다.');
        }
    }

    processGuessResult(jamos, data) {
        const currentRow = this.maxAttempts - data.attemptsLeft - 1;
        
        // 게임 보드 업데이트
        for (let i = 0; i < 6; i++) {
            const tile = document.getElementById(`tile-${currentRow}-${i}`);
            tile.textContent = jamos[i];
            
            const feedback = data.feedback[i];
            if (feedback === 'CORRECT') {
                tile.classList.add('correct');
            } else if (feedback === 'PRESENT') {
                tile.classList.add('present');
            } else {
                tile.classList.add('absent');
            }
        }
        
        // 서버에서 받은 누적 피드백으로 키보드 상태 업데이트
        if (data.cumulativeFeedback) {
            // 누적 피드백을 키보드 상태에 반영
            Object.keys(data.cumulativeFeedback).forEach(jamo => {
                this.keyboardState[jamo] = data.cumulativeFeedback[jamo].toLowerCase();
            });
        } else {
            // 기존 방식 (fallback) - 개별 추측별 피드백 처리
            for (let i = 0; i < 6; i++) {
                const jamo = jamos[i];
                const feedback = data.feedback[i];
                
                if (!this.keyboardState[jamo] || 
                    (this.keyboardState[jamo] === 'absent' && feedback !== 'absent') ||
                    (this.keyboardState[jamo] === 'present' && feedback === 'correct')) {
                    this.keyboardState[jamo] = feedback.toLowerCase();
                }
            }
        }
        
        this.updateKeyboard();
        
        // 게임 상태 업데이트
        this.attemptsLeft = data.attemptsLeft;
        this.updateAttemptsDisplay();
        
        // 입력 초기화
        this.currentJamos = [];
        this.currentIndex = 0;
        this.updateJamoInputs();
        
        // 게임 종료 확인
        if (data.isGameOver) {
            this.isGameOver = true;
            setTimeout(() => {
                this.showResultModal(data.isCleared);
            }, 1500);
        }
    }

    updateKeyboard() {
        document.querySelectorAll('.key').forEach(key => {
            const jamo = key.textContent;
            if (this.keyboardState[jamo]) {
                key.classList.remove('correct', 'present', 'absent');
                key.classList.add(this.keyboardState[jamo]);
            }
        });
    }

    updateAttemptsDisplay() {
        document.getElementById('attempts-left').textContent = `${this.attemptsLeft}회 남음`;
    }

    showResultModal(isWin) {
        const modal = document.getElementById('result-modal');
        const title = document.getElementById('result-title');
        const message = document.getElementById('result-message');
        
        if (isWin) {
            title.textContent = '🎉 성공!';
            message.textContent = `${this.maxAttempts - this.attemptsLeft}번 만에 맞추셨습니다!`;
        } else {
            title.textContent = '😢 실패';
            message.textContent = '다음 기회에 도전해보세요!';
        }
        
        modal.classList.remove('hidden');
    }

    playAgain() {
        document.getElementById('result-modal').classList.add('hidden');
        this.showStartScreenWithNickname();
    }

    showStatsFromModal() {
        document.getElementById('result-modal').classList.add('hidden');
        this.showStatsScreen();
    }

    async loadStats() {
        try {
            const response = await fetch(`/api/game/stats/${encodeURIComponent(this.nickname)}`);
            
            if (response.ok) {
                const stats = await response.json();
                this.displayStats(stats);
            } else {
                console.error('Failed to load stats');
            }
        } catch (error) {
            console.error('Error loading stats:', error);
        }
    }

    displayStats(stats) {
        const statsContent = document.getElementById('stats-content');
        
        const winRate = stats.totalGames > 0 ? (stats.wins / stats.totalGames * 100).toFixed(1) : 0;
        
        statsContent.innerHTML = `
            <div class="stats-container">
                <div class="stats-row">
                    <span class="stats-label">총 게임 수</span>
                    <span class="stats-value">${stats.totalGames}</span>
                </div>
                <div class="stats-row">
                    <span class="stats-label">승리 횟수</span>
                    <span class="stats-value">${stats.wins}</span>
                </div>
                <div class="stats-row">
                    <span class="stats-label">승률</span>
                    <span class="stats-value">${winRate}%</span>
                </div>
                <div class="stats-row">
                    <span class="stats-label">현재 연승</span>
                    <span class="stats-value">${stats.currentStreak}</span>
                </div>
                <div class="stats-row">
                    <span class="stats-label">최대 연승</span>
                    <span class="stats-value">${stats.maxStreak}</span>
                </div>
            </div>
            <div class="distribution">
                <h4>시도 횟수별 분포</h4>
                ${this.createDistributionChart(stats.attemptDistribution, stats.wins)}
            </div>
        `;
    }

    createDistributionChart(distribution, totalWins) {
        let chart = '';
        for (let i = 1; i <= 6; i++) {
            const count = distribution[i] || 0;
            const percentage = totalWins > 0 ? (count / totalWins * 100) : 0;
            
            chart += `
                <div class="dist-row">
                    <div class="dist-label">${i}</div>
                    <div class="dist-bar">
                        <div class="dist-fill" style="width: ${percentage}%"></div>
                    </div>
                    <div class="dist-count">${count}</div>
                </div>
            `;
        }
        return chart;
    }

    async loadDashboard() {
        try {
            const response = await fetch('/api/game/dashboard');
            
            if (response.ok) {
                const dashboard = await response.json();
                this.displayDashboard(dashboard);
            } else {
                console.error('Failed to load dashboard');
                this.displayDashboardError();
            }
        } catch (error) {
            console.error('Error loading dashboard:', error);
            this.displayDashboardError();
        }
    }

    displayDashboard(dashboard) {
        const dashboardContent = document.getElementById('dashboard-content');
        
        if (!dashboard || dashboard.length === 0) {
            dashboardContent.innerHTML = `
                <div class="dashboard-empty">
                    <p>아직 플레이한 사용자가 없습니다.</p>
                </div>
            `;
            return;
        }

        // 스테이지별로 그룹화하고 정렬
        dashboard.sort((a, b) => {
            if (b.maxStageCleared !== a.maxStageCleared) {
                return b.maxStageCleared - a.maxStageCleared;
            }
            return b.totalGames - a.totalGames;
        });

        let dashboardHtml = `
            <div class="dashboard-container">
                <div class="dashboard-header">
                    <span class="rank-header">순위</span>
                    <span class="nickname-header">닉네임</span>
                    <span class="stage-header">최고 스테이지</span>
                    <span class="games-header">총 게임 수</span>
                    <span class="winrate-header">승률</span>
                </div>
        `;

        dashboard.forEach((user, index) => {
            const winRate = user.totalGames > 0 ? (user.wins / user.totalGames * 100).toFixed(1) : 0;
            const rankClass = index < 3 ? `rank-${index + 1}` : '';
            
            dashboardHtml += `
                <div class="dashboard-row ${rankClass}">
                    <span class="rank">${index + 1}</span>
                    <span class="nickname">${user.nickname}</span>
                    <span class="stage">${user.maxStageCleared}</span>
                    <span class="games">${user.totalGames}</span>
                    <span class="winrate">${winRate}%</span>
                </div>
            `;
        });

        dashboardHtml += '</div>';
        dashboardContent.innerHTML = dashboardHtml;
    }

    displayDashboardError() {
        const dashboardContent = document.getElementById('dashboard-content');
        dashboardContent.innerHTML = `
            <div class="dashboard-error">
                <p>대시보드 데이터를 불러오는 중 오류가 발생했습니다.</p>
                <button onclick="location.reload()">새로고침</button>
            </div>
        `;
    }

    resetGame() {
        this.sessionId = null;
        this.currentJamos = [];
        this.gameBoard = [];
        this.isGameOver = false;
        this.keyboardState = {};
        this.attemptsLeft = this.maxAttempts;
        this.currentIndex = 0;
    }
}

// 게임 시작
document.addEventListener('DOMContentLoaded', () => {
    new KordleGame();
});