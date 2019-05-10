import {css} from 'lit-element';

// language=CSS
export const style = css`
    :host {
        display: block;
        width: 100%;
    }
    
    .rule-container {
        display: flex;
        margin: 10px 0;
        width: 100%;
        border-radius: 0;
        padding: 0 0 20px 0;
        border-bottom: 1px solid var(--app-lightgrey-color, #F5F5F5);
    }
    
    .rule-container > * {
        margin-right: 20px;
    }

    .rule-container > or-select-asset-type {
        margin-right: 10px;
    }

    .rule-container > or-select-asset-type or-icon {
        margin-top: 7px;
    }
    
    .rule-container > *:last-child {
        margin-right: 0;
    }
    
    or-icon {
        margin-top: 5px;
    }
    
    .small-icon {
        cursor: pointer;
        fill: rgb(213, 213, 213);
    }
    
    .small-icon:hover {
        fill: var(--app-primary-color);
    }
    
`;