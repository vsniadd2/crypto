let stompClient = null;

function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/crypto', function(message) {
            updateCryptoTable(JSON.parse(message.body));
        });
    });
}

// Функция для обновления таблицы с данными
function updateCryptoTable(data) {
    const tableBody = document.querySelector('#cryptoTable tbody');
    const oldRows = Array.from(tableBody.querySelectorAll('tr'));
    
    data.forEach((coin, index) => {
        const oldRow = oldRows[index];
        const newRow = document.createElement('tr');
        
        // Создаем ячейки с данными
        const cells = [
            coin.name,
            coin.symbol,
            `$${formatNumber(coin.price)}`,
            formatPercentage(coin.percent_change_24h),
            `$${formatNumber(coin.market_cap)}`,
            `$${formatNumber(coin.volume_24h)}`
        ];

        // Если есть старая строка, сравниваем значения и добавляем анимацию
        if (oldRow) {
            const oldCells = oldRow.querySelectorAll('td');
            cells.forEach((value, cellIndex) => {
                const td = document.createElement('td');
                const oldValue = oldCells[cellIndex].textContent;
                
                if (value !== oldValue) {
                    // Добавляем класс для анимации
                    td.innerHTML = value;
                    td.classList.add('highlight-update');
                    // Удаляем класс анимации через 1 секунду
                    setTimeout(() => {
                        td.classList.remove('highlight-update');
                    }, 1000);
                } else {
                    td.innerHTML = value;
                }
                newRow.appendChild(td);
            });
        } else {
            // Если это новая строка, просто добавляем ячейки
            cells.forEach(value => {
                const td = document.createElement('td');
                td.innerHTML = value;
                newRow.appendChild(td);
            });
        }
        
        tableBody.appendChild(newRow);
    });

    // Удаляем лишние строки, если их стало меньше
    while (tableBody.children.length > data.length) {
        tableBody.removeChild(tableBody.lastChild);
    }
}

// Функция для форматирования чисел
function formatNumber(number) {
    return new Intl.NumberFormat('en-US', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(number);
}

// Функция для форматирования процентов
function formatPercentage(number) {
    const formatted = formatNumber(number);
    const color = number >= 0 ? 'text-success' : 'text-danger';
    return `<span class="${color}">${formatted}%</span>`;
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    connect();
}); 