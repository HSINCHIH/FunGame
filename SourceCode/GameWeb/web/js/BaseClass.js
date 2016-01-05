function Rectangle() {
}
Rectangle.prototype =
        {
            left: 0,
            top: 0,
            width: 0,
            height: 0
        };

function Event() {
}
Event.prototype =
        {
            m_EventMap: {},
            AddListener: function (eventType, pFunction)
            {
                if (!this.m_EventMap.hasOwnProperty(eventType))
                {
                    this.m_EventMap[eventType] = [];
                }
                this.m_EventMap[eventType].push(pFunction);
            },
            Dispatch: function (eventType, parameter)
            {
                if (this.m_EventMap.hasOwnProperty(eventType))
                {
                    var aryFunction = ArrayCopy(this.m_EventMap[eventType]);
                    for (var i = 0; i < aryFunction.length; i++)
                    {
                        var pFunction = aryFunction[i];
                        pFunction(parameter);
                    }
                }
            }
        };

function WrapWebSocket() {
    this.Init();
}
WrapWebSocket.prototype =
        {
            m_Event: null,
            m_HostSocket: null,
            ShowMsg: function (msg)
            {
                //console.log(msg);
                //document.getElementById("RTA_SHOWMSG").value += (msg + "\r\n");
            },
            OnOpen: function (event)
            {
                //console.log("open");
                this.m_Event.Dispatch("onOpen", event);
            },
            OnReceive: function (event)
            {
                //console.log("recv : " + event.data);
                this.m_Event.Dispatch("onReceive", event);
            },
            OnError: function (event)
            {
                //console.log("error");
                this.m_Event.Dispatch("onError", event);
            },
            OnClose: function (event)
            {
                //console.log("close");
                this.m_Event.Dispatch("onClose", event);
            },
            Send: function (data)
            {
                this.m_HostSocket.send(data);
            },
            Close: function ()
            {
                this.m_HostSocket.close();
                //var status = base.hostSocket.readyState;
            },
            Connect: function (url)
            {
                try
                {
                    this.m_HostSocket = new WebSocket("ws://" + url);
                    this.m_HostSocket.addEventListener("open", BindWrapper(this, this.OnOpen), false);
                    this.m_HostSocket.addEventListener("message", BindWrapper(this, this.OnReceive), false);
                    this.m_HostSocket.addEventListener("error", BindWrapper(this, this.OnError), false);
                    this.m_HostSocket.addEventListener("close", BindWrapper(this, this.OnClose), false);
                }
                catch (err)
                {
                    var errMsg = err.message;
                    console.log(errMsg);
                }
            },
            Init: function ()
            {
                this.m_Event = new Event();
            }
        };

function WrapWebcam(upNode, width, height) {
    this.Init(upNode, width, height);
}
WrapWebcam.prototype =
        {
            m_Video: null,
            HasGetUserMedia: function ()
            {
                return !!(navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia);
            },
            OnSuccess: function (localMediaStream)
            {
                this.m_Video.src = window.URL.createObjectURL(localMediaStream);
                this.m_Video.onloadedmetadata = function (e)
                {
                    // Ready to go. Do some stuff.
                };
            },
            OnFail: function (e)
            {
                console.log('Rejected!', e);
            },
            Start: function ()
            {
                if (this.HasGetUserMedia())
                {
                    console.log('getUserMedia() is supported in your browser');
                }
                else
                {
                    console.log('getUserMedia() is not supported in your browser');
                }
                navigator.webkitGetUserMedia({video: true, audio: true}, BindWrapper(this, this.OnSuccess), BindWrapper(this, this.OnFail));
            },
            Init: function (upNode, width, height)
            {
                this.m_Video = document.createElement("video");
                this.m_Video.setAttribute("autoplay", "autoplay");
                upNode.appendChild(this.m_Video);
                this.m_Video.width = width;
                this.m_Video.height = height;
            }
        };