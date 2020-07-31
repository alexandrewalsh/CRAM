/**
 * Display animated stars across the home page 
 */
(() => {
    const max_stars = 100;
    const stars = [];
  
    for (let i = 0; i < max_stars; i++) {
        const star = document.createElement('span');
        const size = (Math.floor(Math.random() * 3) + 1);
        star.className = 'star';
        star.style.width = size +'px';
        star.style.height = size + 'px';
        star.style.background = `rgba(255, 255, 177, ${Math.random()})`;
        star.style.top = Math.ceil(Math.random() * 100) + '%';
        star.style.left = Math.ceil(Math.random() * 100) + '%';
        stars.push(star);
        document.body.appendChild(star);
    }
  
    for (let j = 0; j < max_stars * 0.6;  j++) {
        const star = stars[j];
        star.style.animationName = 'glow';
        star.style.animationDelay = (Math.floor(Math.random() * 6) + 1) + 's';
        star.style.animationDuration = (Math.floor(Math.random() * 6) + 1) + 's';
    }
    
})();