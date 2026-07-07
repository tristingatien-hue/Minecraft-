'use strict';

const { contextBridge, ipcRenderer, clipboard } = require('electron');

contextBridge.exposeInMainWorld('api', {
  call: (method, params) => ipcRenderer.invoke('rpc', method, params),
  copyToClipboard: (text) => clipboard.writeText(String(text)),
  onEvent: (handler) => {
    ipcRenderer.on('app-event', (_e, payload) => handler(payload));
  }
});
