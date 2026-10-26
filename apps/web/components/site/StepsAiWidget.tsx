import Script from 'next/script'

/**
 * StepsAI chat agent (floating widget) for the public site. Same snippet the StepsAI dashboard hands out:
 * <script src="https://widget.stepsai.com/widget.js" data-clientid="AGENT_ID" async></script>.
 * Renders nothing until NEXT_PUBLIC_STEPSAI_AGENT_ID is set, so local and preview builds stay clean.
 */
export default function StepsAiWidget() {
  const agentId = process.env.NEXT_PUBLIC_STEPSAI_AGENT_ID
  if (!agentId) return null
  return <Script id="stepsai-widget" src="https://widget.stepsai.com/widget.js" data-clientid={agentId} strategy="lazyOnload" />
}
