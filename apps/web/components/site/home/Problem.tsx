import Section from '../Section'
import ChatBubbles from '../mockups/ChatBubbles'

export default function Problem() {
  return (
    <Section id="problem" tone="white" eyebrow="The problem"
      title="Your flat's WhatsApp group is doing too much."
      lede="Chores, money and reminders get lost between memes and forwards. Oddroof gives each of them a proper home.">
      <ChatBubbles />
    </Section>
  )
}
