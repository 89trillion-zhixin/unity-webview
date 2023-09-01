using System;
using System.Collections.Generic;
using UnityEngine;

namespace Plugins.WebView
{
    public static class WebViewPreload
    {
        private static Dictionary<string, Action> LoadCompleteActionDict { get; } = new Dictionary<String, Action>();
        private static Dictionary<string, Action<int, string>> LoadFailedActionDict { get; } = new Dictionary<String, Action<int, string>>();

        public static void PreloadUrl(string url, Action loadComplete = null, Action<int, string> loadFailed = null)
        {
            if (loadComplete != null)
            {
                if (LoadCompleteActionDict.TryGetValue(url, out var completeAction))
                {
                    completeAction += loadComplete;
                    LoadCompleteActionDict[url] = completeAction;
                }
                else
                {
                    LoadCompleteActionDict[url] = loadComplete;
                }
            }

            if (loadFailed != null)
            {
                if (LoadFailedActionDict.TryGetValue(url, out var failedAction))
                {
                    failedAction += loadFailed;
                    LoadFailedActionDict[url] = failedAction;
                }
                else
                {
                    LoadFailedActionDict[url] = loadFailed;
                }
            }

#if !UNITY_EDITOR && UNITY_ANDROID
        new AndroidJavaObject("net.gree.unitywebview.loader.BrowserLoader").CallStatic("PreloadUrl", url);
#endif
        }

        public static bool IsLoadComplete(string url)
        {
            Debug.Log($"dedpp IsLoadComplete = {url}");
            bool isLoadComplete = false;
#if UNITY_EDITOR
            isLoadComplete = true;
#else
#if UNITY_ANDROID
        isLoadComplete = new AndroidJavaObject("net.gree.unitywebview.loader.BrowserLoader").CallStatic<bool>("IsLoadComplete", url);
#endif
#endif
            return isLoadComplete;
        }

        public static void PreloadLoadComplete(string url)
        {
            if (LoadCompleteActionDict.TryGetValue(url, out var action))
            {
                action?.Invoke();
            }

            LoadCompleteActionDict.Remove(url);
            LoadFailedActionDict.Remove(url);
        }

        public static void PreloadLoadFailed(string url, int errorCode, string description)
        {
            if (LoadFailedActionDict.TryGetValue(url, out var action))
            {
                action?.Invoke(errorCode, description);
            }

            LoadCompleteActionDict.Remove(url);
            LoadFailedActionDict.Remove(url);
        }
    }
}