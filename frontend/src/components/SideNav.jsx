import { NAV_ITEMS } from '../constants/navigation.js'

function SideNav({ active, open, onNavigate }) {
  return (
    <aside className={`side-nav ${open ? 'open' : ''}`}>
      <div className="side-caption">SERVICE MAP</div>
      <nav>
        {NAV_ITEMS.map((item) => (
          <button
            key={item.id}
            className={active === item.id ? 'active' : ''}
            onClick={() => onNavigate(item.id)}
          >
            {item.label}
          </button>
        ))}
      </nav>
    </aside>
  )
}

export default SideNav
