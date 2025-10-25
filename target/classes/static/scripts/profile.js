document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('jwt_token');
    if (!token) {
        window.location.href = '/';
        return;
    }

    // Load profile data
    try {
        const profileData = await api.getProfile();
        document.querySelector('input[name="username"]').value = profileData.username;
        document.querySelector('input[name="email"]').value = profileData.email;
    } catch (error) {
        alert('Error loading profile data');
    }

    // Обработчик для кнопки logout
    const logoutButton = document.querySelector('.logout');
    if (logoutButton) {
        logoutButton.addEventListener('click', async () => {
            try {
                localStorage.removeItem('jwt_token');
                window.location.href = '/';
            } catch (error) {
                console.error('Error during logout:', error);
                alert('An error occurred during logout');
            }
        });
    }

    // Обработчик для переключения вкладок
    const menuItems = document.querySelectorAll('.menu-item');
    const sections = document.querySelectorAll('.profile-section');

    menuItems.forEach(item => {
        item.addEventListener('click', () => {
            // Удаляем активный класс у всех кнопок и секций
            menuItems.forEach(i => i.classList.remove('active'));
            sections.forEach(s => s.classList.remove('active'));

            // Добавляем активный класс выбранной кнопке
            item.classList.add('active');

            // Показываем соответствующую секцию
            const tabId = item.getAttribute('data-tab');
            if (tabId) {
                document.getElementById(tabId).classList.add('active');
            }
        });
    });

    // Handle profile update
    const updateProfileForm = document.querySelector('#general .profile-form');
    updateProfileForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.querySelector('input[name="username"]').value;
        const email = document.querySelector('input[name="email"]').value;

        try {
            const response = await api.updateProfile(username, email);
            if (response.message) {
                alert('Profile updated successfully');
            } else {
                alert(response.error || 'Failed to update profile');
            }
        } catch (error) {
            alert('An error occurred while updating profile');
        }
    });

    // Handle password change
    const changePasswordForm = document.querySelector('#security .profile-form');
    changePasswordForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const currentPassword = document.querySelector('input[placeholder="Enter current password"]').value;
        const newPassword = document.querySelector('input[placeholder="Enter new password"]').value;
        const confirmPassword = document.querySelector('input[placeholder="Confirm new password"]').value;

        if (newPassword !== confirmPassword) {
            alert('New passwords do not match');
            return;
        }

        // TODO: Implement password change API call
        alert('Password change functionality will be implemented soon');
    });

    // Загрузка данных профиля
    loadProfileData();
    // Загрузка избранных криптовалют
    loadFavorites();
});

// Функция получения токена
function getAuthToken() {
    return localStorage.getItem('jwt_token');
}

// Функция проверки токена и редиректа
function checkAuth() {
    const token = getAuthToken();
    if (!token) {
        window.location.href = '/';
    }
    return token;
}

// Функция загрузки данных профиля
async function loadProfileData() {
    try {
        const response = await fetch('/api/profile', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to load profile data');
        }

        const data = await response.json();
        
        // Заполняем поля формы
        document.querySelector('input[name="username"]').value = data.username;
        document.querySelector('input[name="email"]').value = data.email;
    } catch (error) {
        console.error('Error loading profile data:', error);
    }
}

// Функция выхода из аккаунта
async function handleLogout() {
    if (confirm('Are you sure you want to log out?')) {
        // Удаляем токен при выходе
        localStorage.removeItem('jwt_token');
        window.location.href = '/';
    }
}

// В функции проверки статуса аутентификации также используем токен
async function checkAuthStatus() {
    const token = getToken();
    if (token) {
        try {
            const response = await fetch('/profile', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (response.ok) {
                const loginBtn = document.getElementById('loginBtn');
                const profileLink = document.querySelector('.profile-link');
                
                loginBtn.style.display = 'none';
                profileLink.style.display = 'flex';
            } else {
                localStorage.removeItem('jwt_token');
                handleUnauthenticated();
            }
        } catch (error) {
            console.error('Error checking auth status:', error);
            handleUnauthenticated();
        }
    } else {
        handleUnauthenticated();
    }
}

function handleUnauthenticated() {
    const loginBtn = document.getElementById('loginBtn');
    const profileLink = document.querySelector('.profile-link');
    
    loginBtn.style.display = 'block';
    profileLink.style.display = 'none';
}

// Функция загрузки избранных криптовалют
async function loadFavorites() {
    const favoritesGrid = document.querySelector('.favorites-grid');
    if (!favoritesGrid) return;

    try {
        const response = await fetch('/api/favorites', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to load favorites');
        }

        const favorites = await response.json();
        
        favoritesGrid.innerHTML = favorites.map(crypto => `
            <div class="favorite-item">
                <div class="favorite-header">
                    <span class="crypto-icon">₿</span>
                    <span class="crypto-name">${crypto.name}</span>
                    <button class="remove-favorite" onclick="removeFavorite('${crypto.id}')">×</button>
                </div>
                <div class="crypto-price">$${formatNumber(crypto.current_price)}</div>
                <div class="crypto-change ${crypto.price_change_percentage_24h >= 0 ? 'positive' : 'negative'}">
                    ${crypto.price_change_percentage_24h.toFixed(2)}%
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading favorites:', error);
        favoritesGrid.innerHTML = '<p class="error-message">Failed to load favorites</p>';
    }
}

// Функция форматирования чисел
function formatNumber(num) {
    if (!num) return '0.00';
    return num.toLocaleString('en-US', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

// Функция удаления избранной криптовалюты
async function removeFavorite(cryptoId) {
    try {
        const response = await fetch(`/api/favorites/${cryptoId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to remove favorite');
        }
        
        // Перезагружаем список избранных
        loadFavorites();
    } catch (error) {
        console.error('Error removing favorite:', error);
        alert('Failed to remove favorite');
    }
}

// Функция показа уведомлений
function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;

    document.body.appendChild(notification);

    notification.style.position = 'fixed';
    notification.style.top = '20px';
    notification.style.right = '20px';
    notification.style.padding = '15px 25px';
    notification.style.borderRadius = '5px';
    notification.style.backgroundColor = type === 'success' ? '#4CAF50' : '#f44336';
    notification.style.color = 'white';
    notification.style.zIndex = '1000';
    notification.style.transition = 'opacity 0.5s';

    setTimeout(() => {
        notification.style.opacity = '0';
        setTimeout(() => {
            notification.remove();
        }, 500);
    }, 3000);
}

// Обновляем функцию обновления профиля
async function updateProfile(data) {
    const token = checkAuth();
    
    try {
        const response = await fetch('/profile/update', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            showNotification('Profile updated successfully', 'success');
        } else {
            const error = await response.json();
            throw new Error(error.error || 'Failed to update profile');
        }
    } catch (error) {
        console.error('Update error:', error);
        showNotification(error.message, 'error');
    }
}