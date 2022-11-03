using UnityEngine;

namespace Plugins.WebView
{
    public class WebViewDelegator : MonoBehaviour
    {
        public static WebViewDelegator Instance { get; private set; }
        public static bool IsCreated { get; private set; }

        [RuntimeInitializeOnLoadMethod(RuntimeInitializeLoadType.AfterSceneLoad)]
        static void Register()
        {
            CheckInstance();
        }

        private static void CheckInstance()
        {
            if (IsCreated)
            {
                return;
            }

            GameObject go = GameObject.Find("WebViewDelegator");
            if (go != null)
            {
                Instance = go.GetComponent<WebViewDelegator>();
            }

            if (Instance == null)
            {
                go = new GameObject("WebViewDelegator");
                go.hideFlags = HideFlags.DontSave;
                Instance = go.AddComponent<WebViewDelegator>();
            }

            DontDestroyOnLoad(Instance.gameObject);

            IsCreated = true;
        }

        public void PreloadLoadComplete(string url)
        {
            WebViewPreload.PreloadLoadComplete(url);
        }

        public void PreloadLoadFailed(string url, int errorCode, string description)
        {
            WebViewPreload.PreloadLoadFailed(url, errorCode, description);
        }
    }
}