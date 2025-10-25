const API_BASE_URL = '/api';

const api = {
    async login(email, password) {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email, password })
        });
        return response.json();
    },

    async register(username, email, password) {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, email, password })
        });
        return response.json();
    },

    async logout() {
        const response = await fetch(`${API_BASE_URL}/auth/logout`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`
            }
        });
        return response.json();
    },

    async getProfile() {
        const response = await fetch(`${API_BASE_URL}/profile`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`
            }
        });
        return response.json();
    },

    async updateProfile(username, email) {
        const response = await fetch(`${API_BASE_URL}/profile`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`
            },
            body: JSON.stringify({ username, email })
        });
        return response.json();
    },

    // Новый метод для получения данных о криптовалютах
    async getCryptoData() {
        const response = await fetch(`${API_BASE_URL}/currency`, {
            headers: {
                'Content-Type': 'application/json'
            }
        });
        return response.json();
    },

    // Новый метод для получения данных об отдельной монете
    async getSingleCrypto(id) {
        const response = await fetch(`${API_BASE_URL}/currency/${id}`, {
            headers: {
                'Content-Type': 'application/json'
            }
        });
        return response.json();
    }
}; 