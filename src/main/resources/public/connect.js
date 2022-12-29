const authorizeUrl = '/oauth2/authorize';
const connectButtons = document.getElementsByClassName('connect-button');

function setButtonsDisabled(value) {
    console.log(connectButtons);
    for(const button of connectButtons) {
        button.disabled = value;
    }
}

/**
 * This function starts and manages the authorization flow.
 * First it will open a popup with the authorization url.
 * Then, it will listen for postmessages coming from the popup window.
 * If the response is a successful connection  it will close the opneded popup and reload the page.
 */
function startAuthorizationFlow(userId) {
    setButtonsDisabled(true);

    const authorizeUrlWithUserId = `${authorizeUrl}/${userId}`;
    const popup = window.open(authorizeUrlWithUserId, '_blank');

    if(!popup) {
        alert('Your browser blocked the popup. Please enable allow popups for this page.')
        setButtonsDisabled(false);
        return;
    }

    /**
     * This catch the case where the user closes the popup window
     */
    const closedPopupInterval = setInterval(() => {
        if(!popup || popup.closed) {
            setButtonsDisabled(false);

            // Cleanup
            clearInterval(closedPopupInterval);
        }
    }, 1000)

    const postMessageListener = window.addEventListener('message', (event) => {
            if(event.source === popup) {
                if(event.data === 'Success') {
                    console.log('Connection was successful');
                    window.location.reload();
                } else {
                    console.error(`Something went wrong, got ${event.data} back`);
                }

                // Cleanup
                clearInterval(closedPopupInterval);
                window.removeEventListener('message', postMessageListener);
                popup.close();
            }
        });
}