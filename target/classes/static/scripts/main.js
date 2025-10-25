document.addEventListener('DOMContentLoaded', () => {
    // Проверяем авторизацию при загрузке страницы
    checkAuthStatus();

    const modal = document.getElementById('modal');
    const closeModal = document.getElementById('closeModal');
    const registrationForm = document.getElementById('registrationForm');
    const loginForm = document.getElementById('loginForm');
    const toLogin = document.getElementById('toLogin');
    const toRegister = document.getElementById('toRegister');
    const loginBtn = document.getElementById('loginBtn');
    const profileLink = document.querySelector('.profile-link');
    const loginButton = document.getElementById('loginButton');

    // Check if user is already logged in
    const token = localStorage.getItem('jwt_token');
    if (token) {
        loginBtn.style.display = 'none';
        profileLink.style.display = 'block';
    }

    document.querySelector('.registration_button').addEventListener('click', () => {
        modal.style.display = 'block';
    });

    closeModal.addEventListener('click', () => {
        modal.style.display = 'none';
    });

    toLogin.addEventListener('click', () => {
        loginForm.style.display = 'block';
        registrationForm.style.display = 'none';
        toLogin.classList.add('active');
        toRegister.classList.remove('active');
    });

    toRegister.addEventListener('click', () => {
        registrationForm.style.display = 'block';
        loginForm.style.display = 'none';
        toRegister.classList.add('active');
        toLogin.classList.remove('active');
    });

    window.onclick = function (event) {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    }

    let currentPage = 1;
    const rowsPerPage = 25;
    let allCryptoData = [];
    let filteredCryptoData = []; // Добавляем массив для отфильтрованных данных
    let updateInterval; // Для хранения ID интервала

    // Добавляем обработчик поиска
    const searchInput = document.querySelector('.input_search');
    searchInput.addEventListener('input', (e) => {
        const searchTerm = e.target.value.toLowerCase().trim();
        if (searchTerm === '') {
            filteredCryptoData = allCryptoData;
        } else {
            filteredCryptoData = allCryptoData.filter(crypto =>
                crypto.name.toLowerCase().includes(searchTerm) ||
                crypto.symbol.toLowerCase().includes(searchTerm)
            );
        }
        currentPage = 1; // Сбрасываем на первую страницу при поиске
        updateCryptoTable();
        updatePagination();
    });

    // Функция загрузки данных криптовалют
    async function loadCryptoData() {
        try {
            console.log('Fetching new crypto data...');
            const allCryptoData = await api.getCryptoData();
            filteredCryptoData = allCryptoData; // Инициализируем отфильтрованные данные
            console.log('New crypto data received:', new Date().toLocaleTimeString());

            // Обновляем верхнюю панель с топ-9 криптовалютами
            updateTopCryptoBar(allCryptoData.slice(0, 9));

            // Обновляем таблицу с учетом пагинации
            updateCryptoTable();

            // Обновляем пагинацию
            updatePagination();

        } catch (error) {
            console.error('Error loading crypto data:', error);
            // При ошибке останавливаем автообновление
            stopAutoUpdate();
        }
    }

    // Функция запуска автообновления
    function startAutoUpdate() {
        console.log('Starting auto-update with 1-minute interval');
        // Сначала загружаем данные
        loadCryptoData();
        // Затем запускаем интервал обновления
        updateInterval = setInterval(loadCryptoData, 60000); // 60000 мс = 1 минута
    }

    // Функция остановки автообновления
    function stopAutoUpdate() {
        if (updateInterval) {
            clearInterval(updateInterval);
            updateInterval = null;
        }
    }

    // Запускаем автообновление при видимости страницы
    document.addEventListener('visibilitychange', () => {
        if (document.hidden) {
            // Останавливаем обновление, когда страница не видна
            stopAutoUpdate();
        } else {
            // Возобновляем обновление, когда страница становится видна
            startAutoUpdate();
        }
    });

    // Запускаем автообновление при загрузке страницы
    startAutoUpdate();

    // При уходе со страницы останавливаем обновление
    window.addEventListener('beforeunload', () => {
        stopAutoUpdate();
    });

    // Обновление верхней панели
    function updateTopCryptoBar(topCryptos) {
        const nav = document.querySelector('nav');
        nav.innerHTML = topCryptos.map(crypto => `
            <div class="crypto">
                <div class="crypto-name">${crypto.name || ''} (${(crypto.symbol || '').toUpperCase()})</div>
                <div class="crypto-price">$${formatNumber(crypto.price)}</div>
                <div class="crypto-change ${crypto.percent_change_24h >= 0 ? 'positive' : 'negative'}">
                    ${crypto.percent_change_24h ? crypto.percent_change_24h.toFixed(2) : '0.00'}%
                </div>
            </div>
        `).join('');
    }

    // Добавляем обработчик клика по строке криптовалюты
    function addCryptoRowClickHandlers() {
        const cryptoRows = document.querySelectorAll('.crypto-row');
        cryptoRows.forEach(row => {
            row.addEventListener('click', async (e) => {
                // Игнорируем клик по кнопке избранного
                if (e.target.classList.contains('star-button')) return;
                
                const cryptoId = row.getAttribute('data-id');
                if (cryptoId) {
                    await showSingleCryptoDetails(cryptoId);
                }
            });
        });
    }

    // Функция отображения деталей отдельной криптовалюты
    async function showSingleCryptoDetails(id) {
        try {
            const crypto = await api.getSingleCrypto(id);
            
            // Создаем модальное окно для отображения деталей
            const modal = document.createElement('div');
            modal.className = 'crypto-details-modal';
            modal.innerHTML = `
                <div class="crypto-details-content">
                    <div class="crypto-details-header">
                        <h2>${crypto.name} (${crypto.symbol.toUpperCase()})</h2>
                        <button class="close-modal">×</button>
                    </div>
                    <div class="crypto-details-body">
                        <div class="crypto-details-price">
                            <h3>Текущая цена</h3>
                            <p>$${formatNumber(crypto.price)}</p>
                        </div>
                        <div class="crypto-details-changes">
                            <div class="change-item">
                                <span>Изменение за час:</span>
                                <span class="${crypto.percent_change_1h >= 0 ? 'positive' : 'negative'}">
                                    ${crypto.percent_change_1h ? crypto.percent_change_1h.toFixed(2) : '0.00'}%
                                </span>
                            </div>
                            <div class="change-item">
                                <span>Изменение за 24 часа:</span>
                                <span class="${crypto.percent_change_24h >= 0 ? 'positive' : 'negative'}">
                                    ${crypto.percent_change_24h ? crypto.percent_change_24h.toFixed(2) : '0.00'}%
                                </span>
                            </div>
                            <div class="change-item">
                                <span>Изменение за 7 дней:</span>
                                <span class="${crypto.percent_change_7d >= 0 ? 'positive' : 'negative'}">
                                    ${crypto.percent_change_7d ? crypto.percent_change_7d.toFixed(2) : '0.00'}%
                                </span>
                            </div>
                        </div>
                        <div class="crypto-details-volume">
                            <h3>Объем торгов за 24 часа</h3>
                            <p>$${formatNumber(crypto.volume_24h)}</p>
                        </div>
                    </div>
                </div>
            `;

            // Добавляем стили для модального окна
            const style = document.createElement('style');
            style.textContent = `
                .crypto-details-modal {
                    position: fixed;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    background-color: rgba(0, 0, 0, 0.5);
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    z-index: 1000;
                }
                .crypto-details-content {
                    background-color: white;
                    padding: 20px;
                    border-radius: 8px;
                    width: 80%;
                    max-width: 600px;
                }
                .crypto-details-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 20px;
                }
                .close-modal {
                    background: none;
                    border: none;
                    font-size: 24px;
                    cursor: pointer;
                }
                .crypto-details-body {
                    display: grid;
                    gap: 20px;
                }
                .change-item {
                    display: flex;
                    justify-content: space-between;
                    padding: 10px 0;
                    border-bottom: 1px solid #eee;
                }
                .positive {
                    color: #4CAF50;
                }
                .negative {
                    color: #f44336;
                }
            `;
            document.head.appendChild(style);

            // Добавляем обработчик закрытия модального окна
            modal.querySelector('.close-modal').addEventListener('click', () => {
                modal.remove();
            });

            // Закрытие по клику вне модального окна
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    modal.remove();
                }
            });

            document.body.appendChild(modal);
        } catch (error) {
            console.error('Error loading crypto details:', error);
            alert('Failed to load cryptocurrency details');
        }
    }

    // Обновляем функцию updateCryptoTable, чтобы добавить data-id к строкам
    function updateCryptoTable() {
        const startIndex = (currentPage - 1) * rowsPerPage;
        const endIndex = startIndex + rowsPerPage;
        const pageData = filteredCryptoData.slice(startIndex, endIndex);

        const cryptoList = document.getElementById('crypto-list');
        const headerRow = cryptoList.querySelector('.crypto-header');

        cryptoList.innerHTML = '';
        cryptoList.appendChild(headerRow);

        if (pageData.length === 0) {
            const noResultsRow = document.createElement('div');
            noResultsRow.className = 'crypto-row no-results';
            noResultsRow.innerHTML = '<div class="no-results-message">Криптовалюта не найдена</div>';
            cryptoList.appendChild(noResultsRow);
            return;
        }

        pageData.forEach((crypto, index) => {
            const row = document.createElement('div');
            row.className = 'crypto-row';
            row.setAttribute('data-id', crypto.id); // Добавляем ID криптовалюты
            row.innerHTML = `
                <div class="star-button" onclick="toggleFavorite(this)">☆</div>
                <div class="crypto-number">${startIndex + index + 1}</div>
                <div class="crypto-name">${crypto.name || ''} (${(crypto.symbol || '').toUpperCase()})</div>
                <div class="crypto-price">$${formatNumber(crypto.price)}</div>
                <div class="crypto-change ${crypto.percent_change_1h >= 0 ? 'positive' : 'negative'}">
                    ${crypto.percent_change_1h ? crypto.percent_change_1h.toFixed(2) : '0.00'}%
                </div>
                <div class="crypto-change ${crypto.percent_change_24h >= 0 ? 'positive' : 'negative'}">
                    ${crypto.percent_change_24h ? crypto.percent_change_24h.toFixed(2) : '0.00'}%
                </div>
                <div class="crypto-change ${crypto.percent_change_7d >= 0 ? 'positive' : 'negative'}">
                    ${crypto.percent_change_7d ? crypto.percent_change_7d.toFixed(2) : '0.00'}%
                </div>
                <div class="crypto-volume">$${formatNumber(crypto.volume_24h)}</div>
            `;
            cryptoList.appendChild(row);
        });

        // Добавляем обработчики клика после обновления таблицы
        addCryptoRowClickHandlers();
    }

    // Обновление пагинации
    function updatePagination() {
        const totalPages = Math.ceil(filteredCryptoData.length / rowsPerPage);
        const pageNumbersContainer = document.getElementById('page-numbers');
        pageNumbersContainer.innerHTML = '';

        if (totalPages === 0) return;

        for (let i = 1; i <= totalPages; i++) {
            const pageNumber = document.createElement('span');
            pageNumber.textContent = i;
            pageNumber.className = `page-number ${i === currentPage ? 'active' : ''}`;
            pageNumber.onclick = () => {
                currentPage = i;
                updateCryptoTable();
                updatePagination();
            };
            pageNumbersContainer.appendChild(pageNumber);
        }
    }

    // Функция форматирования чисел
    function formatNumber(num) {
        if (!num) return '0.00';

        // Для цен криптовалют (если число меньше миллиона)
        if (num < 1000000) {
            // Для чисел меньше 1, показываем больше десятичных знаков
            if (num < 1) {
                return num.toFixed(8);
            }
            // Для остальных цен показываем 2 десятичных знака
            return num.toLocaleString('en-US', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            });
        }

        // Для больших чисел (объемы, капитализация и т.д.)
        if (num >= 1e9) {
            return (num / 1e9).toFixed(2) + 'B';
        }
        if (num >= 1e6) {
            return (num / 1e6).toFixed(2) + 'M';
        }
        if (num >= 1e3) {
            return (num / 1e3).toFixed(2) + 'K';
        }

        return num.toFixed(2);
    }

    // Функция смены страницы
    window.changePage = function(direction) {
        const totalPages = Math.ceil(filteredCryptoData.length / rowsPerPage);
        const newPage = currentPage + direction;

        if (newPage >= 1 && newPage <= totalPages) {
            currentPage = newPage;
            updateCryptoTable();
            updatePagination();
        }
    }

    function toggleFavorite(star) {
        star.classList.toggle('favorited');
        star.textContent = star.classList.contains('favorited') ? '★' : '☆'; // Меняем символ звезды
    }

    const newsData = [
        {
            id: 1,
            type: 'type1',
            title: 'Новость 1 тип 1',
            text: 'Описание новости 1',
            time: '12:00',
            image: 'https://via.placeholder.com/150'
        },
        {
            id: 2,
            type: 'type2',
            title: 'Новость 2 тип 2',
            text: 'Описание новости 2',
            time: '12:30',
            image: 'https://via.placeholder.com/150'
        },
        {
            id: 3,
            type: 'type3',
            title: 'Новость 3 тип 3',
            text: 'Описание новости 3',
            time: '13:00',
            image: 'https://via.placeholder.com/150'
        },
        {
            id: 4,
            type: 'type1',
            title: 'Новость 4 тип 1',
            text: 'Описание новости 4',
            time: '13:30',
            image: 'https://via.placeholder.com/150'
        },
        {
            id: 5,
            type: 'type2',
            title: 'Новость 5 тип 2',
            text: 'Описание новости 5',
            time: '14:00',
            image: 'https://via.placeholder.com/150'
        },
        {
            id: 6,
            type: 'type3',
            title: 'Новость 6 тип 3',
            text: 'Описание новости 6',
            time: '14:30',
            image: 'https://via.placeholder.com/150'
        },
    ];

    const buttons = document.querySelectorAll('.button');
    const newsGrid = document.getElementById('newsGrid');

    buttons.forEach(button => {
        button.addEventListener('click', () => {
            // Удаляем класс active у всех кнопок
            buttons.forEach(btn => btn.classList.remove('active'));

            // Добавляем класс active к нажатой кнопке
            button.classList.add('active');

            // Фильтруем новости
            const type = button.getAttribute('data-type');
            filterNews(type);
        });
    });

    function filterNews(type) {
        newsGrid.innerHTML = ''; // Очищаем текущие новости
        const filteredNews = type === 'all' ? newsData : newsData.filter(news => news.type === type);
        filteredNews.forEach(news => {
            const newsItem = document.createElement('div');
            newsItem.className = 'news-item';
            newsItem.innerHTML = `
                    <img src="${news.image}" alt="${news.title}">
                    <div class="news-title">${news.title}</div>
                    <div class="news-text">${news.text}</div>
                    <div class="news-time">${news.time}</div>
                `;
            newsGrid.appendChild(newsItem);
        });
    }

    // Инициализация с отображением всех новостей
    filterNews('all');

    // Handle login
    loginButton.addEventListener('click', async () => {
        const email = document.getElementById('loginEmail').value;
        const password = document.getElementById('loginPassword').value;

        try {
            const response = await api.login(email, password);
            if (response.success) {
                localStorage.setItem('jwt_token', response.token);
                loginBtn.style.display = 'none';
                profileLink.style.display = 'block';
                modal.style.display = 'none';
                window.location.reload();
            } else {
                alert(response.error || 'Login failed');
            }
        } catch (error) {
            alert('An error occurred during login');
        }
    });

    // Handle registration
    const registerButton = registrationForm.querySelector('.action-button');
    registerButton.addEventListener('click', async () => {
        const username = document.getElementById('registerUsername').value;
        const email = document.getElementById('registerEmail').value;
        const password = document.getElementById('registerPassword').value;

        try {
            const response = await api.register(username, email, password);
            if (response.success) {
                alert('Registration successful! Please login.');
                toLogin.click();
            } else {
                alert(response.error || 'Registration failed');
            }
        } catch (error) {
            alert('An error occurred during registration');
        }
    });

    // Handle logout
    const logoutButton = document.querySelector('.logout');
    if (logoutButton) {
        logoutButton.addEventListener('click', async () => {
            try {
                await api.logout();
                localStorage.removeItem('jwt_token');
                window.location.href = '/';
            } catch (error) {
                alert('An error occurred during logout');
            }
        });
    }

    // Функция проверки статуса авторизации
    async function checkAuthStatus() {
        const token = localStorage.getItem('jwt_token');
        if (token) {
            loginBtn.style.display = 'none';
            profileLink.style.display = 'block';
        } else {
            loginBtn.style.display = 'block';
            profileLink.style.display = 'none';
        }
    }
});